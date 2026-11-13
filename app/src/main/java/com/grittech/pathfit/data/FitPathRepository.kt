package com.grittech.pathfit.data

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.grittech.pathfit.R
import com.grittech.pathfit.domain.LocalCatalog
import com.grittech.pathfit.domain.asStringKeyMap
import com.grittech.pathfit.domain.fitBool
import com.grittech.pathfit.domain.fitInt
import com.grittech.pathfit.domain.fitMap
import com.grittech.pathfit.domain.fitNumber
import com.grittech.pathfit.domain.fitString
import com.grittech.pathfit.domain.fitStringList
import com.grittech.pathfit.domain.localizedName
import com.grittech.pathfit.model.BootstrapResult
import com.grittech.pathfit.model.CommonCard
import com.grittech.pathfit.model.CommonItem
import com.grittech.pathfit.model.DishHit
import com.grittech.pathfit.model.IngredientHit
import com.grittech.pathfit.model.MealLog
import com.grittech.pathfit.model.OnboardingDraft
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.model.RecipeDraft
import com.grittech.pathfit.model.Targets
import com.grittech.pathfit.model.UsualItem
import com.grittech.pathfit.model.UsualMeal
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val message: String) : Outcome<Nothing>
}

class FitPathRepository(context: Context) {
    private val appContext = context.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-south1")
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val drafts = DraftStore(appContext)
    val session = SessionPrefs(appContext)

    fun currentUid(): String? = auth.currentUser?.uid
    fun currentDisplayName(): String? = auth.currentUser?.displayName
    fun isOnline(): Boolean {
        val manager = appContext.getSystemService(ConnectivityManager::class.java) ?: return true
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun signInWithGoogle(activity: Activity): Outcome<Unit> = try {
        val webClientId = activity.getString(R.string.default_web_client_id)
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val google = GoogleIdTokenCredential.createFrom(result.credential.data)
        val credential = GoogleAuthProvider.getCredential(google.idToken, null)
        auth.signInWithCredential(credential).await()
        Outcome.Ok(Unit)
    } catch (cancelled: GetCredentialCancellationException) {
        Outcome.Err("Sign-in cancelled")
    } catch (error: Exception) {
        Outcome.Err(error.fitMessage())
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount(activity: Activity): Outcome<Unit> {
        val signed = signInWithGoogle(activity)
        if (signed is Outcome.Err) return signed
        return try {
            call("userDeleteAccount", emptyMap(), 40)
            auth.currentUser?.delete()?.await()
            auth.signOut()
            Outcome.Ok(Unit)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun bootstrap(): Outcome<BootstrapResult> = try {
        val data = call("userBootstrap", emptyMap(), 40)
        Outcome.Ok(parseBootstrap(data))
    } catch (error: Exception) {
        if (error.fitMessage().contains("ENTITLEMENT_INITIALIZING", ignoreCase = true)) {
            try {
                val retry = call("userBootstrap", emptyMap(), 40)
                Outcome.Ok(parseBootstrap(retry))
            } catch (again: Exception) {
                Outcome.Err(again.fitMessage())
            }
        } else {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun completeOnboarding(draft: OnboardingDraft): Outcome<Targets> {
        val uid = currentUid() ?: return Outcome.Err("Sign in before calculating targets")
        return try {
            val user = db.collection("users").document(uid)
            user.set(
                mapOf(
                    "displayName" to draft.displayName.trim(),
                    "onboardingStep" to "preview",
                ),
                SetOptions.merge(),
            ).await()
            user.collection("profile").document("main").set(draft.toProfileMap(), SetOptions.merge()).await()
            val response = call("onboardingComplete", mapOf("needsDoctorPrompt" to false), 40)
            val targets = parseTargets(response.fitMap("targets").ifEmpty { response })
            Outcome.Ok(targets)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun refreshTargets(): Outcome<Targets> = try {
        val response = call("targetsRefresh", emptyMap(), 40)
        Outcome.Ok(parseTargets(response.fitMap("targets").ifEmpty { response }))
    } catch (error: Exception) {
        Outcome.Err(error.fitMessage())
    }

    suspend fun previewTargets(): Outcome<Targets> = try {
        val response = call("targetsPreview", emptyMap(), 40)
        Outcome.Ok(parseTargets(response.fitMap("targets").ifEmpty { response }))
    } catch (error: Exception) {
        Outcome.Err(error.fitMessage())
    }

    fun listenTargets(uid: String, onUpdate: (Targets?) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("profile").document("targets")
            .addSnapshotListener { snapshot, _ ->
                val data = snapshot?.data?.mapValues { it.value } ?: run {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                onUpdate(parseTargets(data))
            }

    fun listenInsights(uid: String, onTip: (String?) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("profile").document("insights")
            .addSnapshotListener { snapshot, _ ->
                onTip(parseCoachTip(snapshot?.data))
            }

    fun listenUsuals(uid: String, onUpdate: (List<UsualMeal>) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("usualMeals")
            .addSnapshotListener { snapshot, _ ->
                val meals = snapshot?.documents?.mapNotNull { doc ->
                    parseUsual(doc.id, doc.data ?: return@mapNotNull null)
                }.orEmpty()
                onUpdate(meals)
            }

    fun listenLogs(uid: String, dayKey: String, onUpdate: (List<MealLog>) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("mealLogs")
            .whereEqualTo("dayKey", dayKey)
            .addSnapshotListener { snapshot, _ ->
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    parseLog(doc.id, doc.data ?: return@mapNotNull null)
                }.orEmpty()
                onUpdate(logs)
            }

    fun listenDrinks(uid: String, dayKey: String, onMl: (Int) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("drinkLogs")
            .whereEqualTo("dayKey", dayKey)
            .addSnapshotListener { snapshot, _ ->
                val ml = snapshot?.documents?.sumOf { doc ->
                    val data = doc.data ?: return@sumOf 0
                    val deleted = data["deletedAt"] != null
                    val type = data["type"] as? String
                    if (deleted || type != "water") 0 else (data["amountMl"] as? Number)?.toInt() ?: 0
                } ?: 0
                onMl(ml)
            }

    suspend fun suggestAll(): Outcome<Map<String, List<CommonCard>>> = try {
        val response = call("commonMealsSuggestAll", mapOf("limit" to 6), 60)
        val bySlot = response.fitMap("bySlot")
        val parsed = listOf("breakfast", "lunch", "dinner", "snack").associateWith { slot ->
            val slotMap = bySlot.fitMap(slot)
            parseCards(slotMap["cards"], slot)
        }
        if (parsed.values.all { it.isEmpty() }) Outcome.Ok(LocalCatalog.staples())
        else Outcome.Ok(parsed)
    } catch (_: Exception) {
        try {
            val slots = listOf("breakfast", "lunch", "dinner", "snack")
            val combined = slots.associateWith { slot ->
                val response = call("commonMealsSuggest", mapOf("mealSlot" to slot, "limit" to 4), 40)
                parseCards(response["cards"], slot)
            }
            if (combined.values.all { it.isEmpty() }) Outcome.Ok(LocalCatalog.staples())
            else Outcome.Ok(combined)
        } catch (error: Exception) {
            Outcome.Ok(LocalCatalog.staples())
        }
    }

    suspend fun suggestSlot(slot: String): Outcome<List<CommonCard>> = try {
        val response = call("commonMealsSuggest", mapOf("mealSlot" to slot, "limit" to 4), 40)
        val cards = parseCards(response["cards"], slot)
        Outcome.Ok(cards.ifEmpty { LocalCatalog.staples()[slot].orEmpty() })
    } catch (_: Exception) {
        Outcome.Ok(LocalCatalog.staples()[slot].orEmpty())
    }

    suspend fun saveUsualFromCards(cards: List<CommonCard>): Outcome<Int> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        return try {
            val batch = db.batch()
            cards.forEach { card ->
                val ref = db.collection("users").document(uid).collection("usualMeals").document(UUID.randomUUID().toString())
                batch.set(ref, card.toUsualMap())
            }
            batch.commit().await()
            Outcome.Ok(cards.size)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun saveCustomUsual(name: String, slot: String, items: List<PlateItem>, logToday: Boolean, dayKey: String): Outcome<String> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        if (items.isEmpty()) return Outcome.Err("Add at least one dish")
        if (items.size > 12) return Outcome.Err("A usual can hold up to 12 items")
        return try {
            val id = UUID.randomUUID().toString()
            val ref = db.collection("users").document(uid).collection("usualMeals").document(id)
            ref.set(plateToUsualMap(name, slot, items)).await()
            if (logToday) {
                logUsual(id, dayKey)
            }
            Outcome.Ok(id)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun logUsual(usualId: String, dayKey: String): Outcome<List<String>> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        return try {
            val snap = db.collection("users").document(uid).collection("usualMeals").document(usualId).get().await()
            val data = snap.data ?: return Outcome.Err("Usual not found")
            val usual = parseUsual(snap.id, data)
            writeLogs(uid, dayKey, usual.mealSlot, usual.name, usual.id, usual.items)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun logItems(dayKey: String, slot: String, title: String, items: List<UsualItem>, usualId: String?): Outcome<List<String>> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        return try {
            writeLogs(uid, dayKey, slot, title, usualId, items)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun undoLogs(ids: List<String>) {
        val uid = currentUid() ?: return
        val batch = db.batch()
        ids.forEach { id ->
            val ref = db.collection("users").document(uid).collection("mealLogs").document(id)
            batch.update(ref, mapOf("deletedAt" to FieldValue.serverTimestamp(), "status" to "skipped"))
        }
        batch.commit().await()
    }

    suspend fun addGlass(dayKey: String): Outcome<Unit> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        return try {
            db.collection("users").document(uid).collection("drinkLogs").document(UUID.randomUUID().toString()).set(
                mapOf(
                    "schemaVersion" to 1,
                    "type" to "water",
                    "amountMl" to 250,
                    "dayKey" to dayKey,
                    "loggedAt" to Timestamp.now(),
                    "clientMutationId" to UUID.randomUUID().toString(),
                ),
            ).await()
            Outcome.Ok(Unit)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun searchDishes(query: String): List<DishHit> {
        val local = filterDishes(LocalCatalog.dishes(), query)
        return try {
            val response = call("dishSearch", mapOf("query" to query, "limit" to 20), 40)
            val remote = parseDishes(response)
            if (remote.isEmpty()) local else remote
        } catch (_: Exception) {
            local
        }
    }

    suspend fun searchIngredients(query: String, category: String): List<IngredientHit> {
        val remote = try {
            db.collection("ingredients").limit(80).get().await().documents.mapNotNull { doc ->
                parseIngredient(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (_: Exception) {
            emptyList()
        }
        val source = remote.ifEmpty { LocalCatalog.ingredients() }
        return source.filter { hit ->
            val categoryOk = category == "all" || hit.category == category
            val text = query.isBlank() || hit.nameEn.contains(query, true) || hit.detail.contains(query, true)
            categoryOk && text
        }
    }

    suspend fun saveRecipe(draft: RecipeDraft): Outcome<String> {
        val uid = currentUid() ?: return Outcome.Err("Sign in required")
        if (draft.name.isBlank()) return Outcome.Err("Name the recipe")
        if (draft.lines.isEmpty()) return Outcome.Err("Add at least one ingredient")
        return try {
            val id = UUID.randomUUID().toString()
            val kcal = draft.lines.sumOf { it.kcal }
            val protein = draft.lines.sumOf { it.proteinG }
            val carbs = draft.lines.sumOf { it.carbsG }
            val fat = draft.lines.sumOf { it.fatG }
            db.collection("users").document(uid).collection("userRecipes").document(id).set(
                mapOf(
                    "schemaVersion" to 1,
                    "entryMode" to "recipe",
                    "name" to draft.name.trim(),
                    "mealSlots" to listOf(draft.mealSlot),
                    "ingredients" to draft.lines.map { line ->
                        mapOf(
                            "ingredientId" to line.ingredientId,
                            "name" to line.name,
                            "grams" to line.grams,
                            "kcal" to line.kcal,
                            "proteinG" to line.proteinG,
                            "carbsG" to line.carbsG,
                            "fatG" to line.fatG,
                        )
                    },
                    "nutritionPerServing" to mapOf(
                        "kcal" to kcal,
                        "proteinG" to protein,
                        "carbsG" to carbs,
                        "fatG" to fat,
                    ),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Outcome.Ok(id)
        } catch (error: Exception) {
            Outcome.Err(error.fitMessage())
        }
    }

    suspend fun refreshInsights(): Outcome<Unit> = try {
        call("insightsRefresh", emptyMap(), 40)
        Outcome.Ok(Unit)
    } catch (error: Exception) {
        Outcome.Err(error.fitMessage())
    }

    private suspend fun writeLogs(
        uid: String,
        dayKey: String,
        slot: String,
        title: String,
        usualId: String?,
        items: List<UsualItem>,
    ): Outcome<List<String>> {
        if (items.isEmpty()) return Outcome.Err("Nothing to log")
        val batch = db.batch()
        val ids = items.map { UUID.randomUUID().toString() }
        items.zip(ids).forEach { (item, id) ->
            val nutrition = resolveNutrition(item)
            val ref = db.collection("users").document(uid).collection("mealLogs").document(id)
            val payload = hashMapOf<String, Any?>(
                "schemaVersion" to 1,
                "clientMutationId" to UUID.randomUUID().toString(),
                "dayKey" to dayKey,
                "mealSlot" to slot,
                "status" to "eaten",
                "title" to item.name.ifBlank { title },
                "episodeTitle" to title,
                "dishId" to item.dishId,
                "userRecipeId" to item.userRecipeId,
                "portion" to item.portion,
                "oilLevel" to item.oilLevel,
                "unit" to item.unit,
                "count" to item.count,
                "usualMealId" to usualId,
                "loggedAt" to Timestamp.now(),
                "nutritionSnapshot" to mapOf(
                    "kcal" to nutrition.kcal,
                    "proteinG" to nutrition.proteinG,
                    "carbsG" to nutrition.carbsG,
                    "fatG" to nutrition.fatG,
                ),
            ).filterValues { it != null }
            batch.set(ref, payload)
        }
        if (usualId != null) {
            val usual = db.collection("users").document(uid).collection("usualMeals").document(usualId)
            batch.update(
                usual,
                mapOf(
                    "useCount" to FieldValue.increment(1),
                    "lastUsedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }
        batch.commit().await()
        return Outcome.Ok(ids)
    }

    private suspend fun resolveNutrition(item: UsualItem): com.grittech.pathfit.model.MacroTotals {
        val dishId = item.dishId
        if (dishId != null && item.kcalPerServing <= 0.0) {
            val remote = runCatching {
                db.collection("dishes").document(dishId).get().await().data
            }.getOrNull()
            if (remote != null) {
                val nutrition = remote.fitMap("nutritionPerServing").ifEmpty { remote }
                val kcal = nutrition.fitNumber("kcal") ?: nutrition.fitNumber("calories") ?: 0.0
                return com.grittech.pathfit.domain.CalorieMath.line(
                    kcal,
                    nutrition.fitNumber("proteinG") ?: 0.0,
                    nutrition.fitNumber("carbsG") ?: 0.0,
                    nutrition.fitNumber("fatG") ?: 0.0,
                    remote.fitNumber("addedFatKcalPerServing") ?: item.addedFatKcal,
                    item.portion,
                    item.oilLevel,
                )
            }
        }
        return com.grittech.pathfit.domain.CalorieMath.line(
            item.kcalPerServing,
            item.proteinPer,
            item.carbsPer,
            item.fatPer,
            item.addedFatKcal,
            item.portion,
            item.oilLevel,
        )
    }

    private suspend fun call(name: String, body: Map<String, Any?>, timeoutSec: Long): Map<String, Any?> {
        val payload = HashMap<String, Any>()
        body.forEach { (key, value) -> if (value != null) payload[key] = value }
        val result = functions.getHttpsCallable(name)
            .withTimeout(timeoutSec, TimeUnit.SECONDS)
            .call(payload)
            .await()
        return result.getData().asStringKeyMap()
    }

    private fun parseBootstrap(data: Map<String, Any?>): BootstrapResult {
        val entitlement = data.fitMap("entitlement")
        return BootstrapResult(
            uid = data.fitString("uid") ?: currentUid().orEmpty(),
            onboardingStatus = data.fitString("onboardingStatus") ?: "pending",
            subscriptionStatus = entitlement.fitString("subscriptionStatus") ?: "none",
        )
    }

    private fun OnboardingDraft.toProfileMap(): Map<String, Any> = mapOf(
        "dateOfBirth" to dateOfBirth,
        "sexAtBirth" to sexAtBirth,
        "heightCm" to heightCm,
        "weightKg" to weightKg,
        "targetWeightKg" to targetWeightKg,
        "activityLevel" to activityLevel,
        "goal" to goal,
        "conditionKeys" to conditionKeys,
        "allergenFlags" to allergenFlags,
        "dietTypes" to dietTypes,
        "timezone" to timezone,
        "countryCode" to countryCode,
        "regionKey" to regionKey,
        "units" to mapOf("mass" to "kg", "height" to heightUnit),
        "schemaVersion" to 1,
        "updatedAt" to FieldValue.serverTimestamp(),
    )

    private fun CommonCard.toUsualMap(): Map<String, Any?> = mapOf(
        "schemaVersion" to 1,
        "clientMutationId" to UUID.randomUUID().toString(),
        "mealSlot" to mealSlot,
        "name" to nameEn,
        "items" to items.map { it.toUsualItemMap() },
        "source" to "common",
        "commonMealTemplateId" to templateId,
        "useCount" to 0,
        "lastUsedAt" to null,
        "pinned" to true,
        "status" to "active",
        "revision" to 1,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
    ).filterValues { it != null }

    private fun CommonItem.toUsualItemMap(): Map<String, Any?> = mapOf(
        "dishId" to dishId,
        "portion" to defaultPortion,
        "oilLevel" to if (addedFatKcal > 0) "normal" else null,
        "unit" to unit,
        "count" to defaultPortion,
        "name" to nameEn,
        "kcalPerServing" to if (defaultPortion == 0.0) estimateKcal.toDouble() else estimateKcal / defaultPortion,
        "proteinPer" to proteinG,
        "carbsPer" to carbsG,
        "fatPer" to fatG,
        "addedFatKcal" to addedFatKcal,
    ).filterValues { it != null }

    private fun plateToUsualMap(name: String, slot: String, items: List<PlateItem>): Map<String, Any?> = mapOf(
        "schemaVersion" to 1,
        "clientMutationId" to UUID.randomUUID().toString(),
        "mealSlot" to slot,
        "name" to name,
        "items" to items.map { item ->
            mapOf(
                "dishId" to item.dishId,
                "userRecipeId" to item.userRecipeId,
                "portion" to item.portion,
                "oilLevel" to item.oilLevel,
                "unit" to item.unit,
                "count" to item.portion,
                "name" to item.name,
                "kcalPerServing" to item.kcalPerServing,
                "proteinPer" to item.proteinPer,
                "carbsPer" to item.carbsPer,
                "fatPer" to item.fatPer,
                "addedFatKcal" to item.addedFatKcal,
            ).filterValues { it != null }
        },
        "source" to "user",
        "commonMealTemplateId" to null,
        "useCount" to 0,
        "lastUsedAt" to null,
        "pinned" to true,
        "status" to "active",
        "revision" to 1,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
    ).filterValues { it != null }

    private fun parseTargets(data: Map<String, Any?>): Targets = Targets(
        status = data.fitString("status") ?: "incomplete",
        blockReasons = data.fitStringList("blockReasons"),
        calorieTarget = data.fitInt("calorieTarget"),
        floorApplied = data.fitBool("floorApplied"),
        adjustments = data.fitStringList("adjustments"),
        goalEffective = data.fitString("goalEffective"),
        goalRequested = data.fitString("goalRequested"),
        proteinG = data.fitInt("proteinG"),
        carbsG = data.fitInt("carbsG"),
        fatG = data.fitInt("fatG"),
        proteinPct = data.fitInt("proteinPct"),
        carbsPct = data.fitInt("carbsPct"),
        fatPct = data.fitInt("fatPct"),
        bmr = data.fitNumber("bmr"),
        tdee = data.fitNumber("tdee"),
        goalDelta = data.fitNumber("goalDelta"),
        appliedGoalDelta = data.fitNumber("appliedGoalDelta"),
        weeklyPaceKg = data.fitNumber("weeklyPaceKg"),
        method = data.fitString("method"),
        clinicalPending = data.fitBool("clinicalPending"),
    )

    private fun parseCards(raw: Any?, slot: String): List<CommonCard> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { entry ->
            val map = entry.asStringKeyMap()
            if (map.isEmpty()) return@mapNotNull null
            val items = (map["items"] as? List<*>)?.mapNotNull { item ->
                val row = item.asStringKeyMap()
                val dishId = row.fitString("dishId") ?: return@mapNotNull null
                CommonItem(
                    dishId = dishId,
                    nameEn = localizedName(row),
                    defaultPortion = row.fitNumber("defaultPortion") ?: 1.0,
                    estimateKcal = row.fitInt("estimateKcal") ?: 0,
                    unit = row.fitString("unit") ?: "serving",
                    proteinG = row.fitNumber("proteinG") ?: 0.0,
                    carbsG = row.fitNumber("carbsG") ?: 0.0,
                    fatG = row.fitNumber("fatG") ?: 0.0,
                    addedFatKcal = row.fitNumber("addedFatKcal") ?: 0.0,
                )
            }.orEmpty()
            CommonCard(
                templateId = map.fitString("templateId") ?: UUID.randomUUID().toString(),
                nameEn = localizedName(map),
                nameUr = map.fitMap("name").fitString("ur"),
                items = items,
                mealSlot = map.fitString("mealSlot") ?: slot,
                source = map.fitString("source") ?: "country",
            )
        }
    }

    private fun parseUsual(id: String, data: Map<String, Any?>): UsualMeal {
        val items = (data["items"] as? List<*>)?.mapNotNull { raw ->
            val row = raw.asStringKeyMap()
            if (row.isEmpty()) return@mapNotNull null
            UsualItem(
                dishId = row.fitString("dishId"),
                userRecipeId = row.fitString("userRecipeId"),
                name = row.fitString("name") ?: "",
                portion = row.fitNumber("portion") ?: row.fitNumber("count") ?: 1.0,
                oilLevel = row.fitString("oilLevel"),
                unit = row.fitString("unit") ?: "serving",
                count = row.fitNumber("count") ?: 1.0,
                kcalPerServing = row.fitNumber("kcalPerServing") ?: 0.0,
                proteinPer = row.fitNumber("proteinPer") ?: 0.0,
                carbsPer = row.fitNumber("carbsPer") ?: 0.0,
                fatPer = row.fitNumber("fatPer") ?: 0.0,
                addedFatKcal = row.fitNumber("addedFatKcal") ?: 0.0,
            )
        }.orEmpty()
        val last = data["lastUsedAt"]
        val lastMs = when (last) {
            is Timestamp -> last.toDate().time
            is Number -> last.toLong()
            else -> 0L
        }
        return UsualMeal(
            id = id,
            mealSlot = data.fitString("mealSlot") ?: "lunch",
            name = data.fitString("name") ?: "Usual",
            items = items,
            pinned = data.fitBool("pinned"),
            status = data.fitString("status") ?: "active",
            lastUsedAt = lastMs,
            source = data.fitString("source") ?: "user",
            templateId = data.fitString("commonMealTemplateId"),
        )
    }

    private fun parseLog(id: String, data: Map<String, Any?>): MealLog {
        val nutrition = data.fitMap("nutritionSnapshot")
        val logged = data["loggedAt"]
        val loggedMs = when (logged) {
            is Timestamp -> logged.toDate().time
            is Number -> logged.toLong()
            else -> 0L
        }
        return MealLog(
            id = id,
            dayKey = data.fitString("dayKey") ?: "",
            mealSlot = data.fitString("mealSlot") ?: "snack",
            status = data.fitString("status") ?: "eaten",
            title = data.fitString("episodeTitle") ?: data.fitString("title") ?: "Meal",
            kcal = nutrition.fitInt("kcal") ?: 0,
            proteinG = nutrition.fitNumber("proteinG") ?: 0.0,
            carbsG = nutrition.fitNumber("carbsG") ?: 0.0,
            fatG = nutrition.fitNumber("fatG") ?: 0.0,
            loggedAt = loggedMs,
            deleted = data["deletedAt"] != null,
            usualMealId = data.fitString("usualMealId"),
        )
    }

    private fun parseDishes(response: Map<String, Any?>): List<DishHit> {
        val raw = (response["dishes"] ?: response["results"] ?: response["items"] ?: response["hits"]) as? List<*>
            ?: return emptyList()
        return raw.mapNotNull { entry ->
            val map = entry.asStringKeyMap()
            val id = map.fitString("dishId") ?: map.fitString("id") ?: return@mapNotNull null
            val nutrition = map.fitMap("nutritionPerServing").ifEmpty { map }
            DishHit(
                dishId = id,
                nameEn = localizedName(map),
                detail = map.fitString("detail") ?: map.fitStringList("tags").joinToString(" · "),
                kcal = nutrition.fitNumber("kcal") ?: nutrition.fitNumber("calories") ?: 0.0,
                proteinG = nutrition.fitNumber("proteinG") ?: 0.0,
                carbsG = nutrition.fitNumber("carbsG") ?: 0.0,
                fatG = nutrition.fitNumber("fatG") ?: 0.0,
                addedFatKcal = map.fitNumber("addedFatKcalPerServing") ?: 0.0,
                tags = map.fitStringList("tags"),
                preps = map.fitStringList("preps"),
                unit = map.fitString("unit") ?: "serving",
            )
        }
    }

    private fun parseIngredient(id: String, data: Map<String, Any?>): IngredientHit {
        val nutrition = data.fitMap("nutritionPer100g").ifEmpty { data.fitMap("per100g") }
        return IngredientHit(
            id = id,
            nameEn = localizedName(data),
            detail = data.fitString("detail") ?: "",
            category = data.fitString("category") ?: "all",
            kcalPer100g = nutrition.fitNumber("kcal") ?: nutrition.fitNumber("calories") ?: 0.0,
            proteinPer100g = nutrition.fitNumber("proteinG") ?: 0.0,
            carbsPer100g = nutrition.fitNumber("carbsG") ?: 0.0,
            fatPer100g = nutrition.fitNumber("fatG") ?: 0.0,
        )
    }

    private fun parseCoachTip(data: Map<String, Any>?): String? {
        if (data == null) return null
        val tips = data["tips"] as? List<*> ?: return data["today_coach"] as? String
        val parsed = tips.mapNotNull { tip ->
            when (tip) {
                is String -> tip to listOf("today_coach")
                is Map<*, *> -> {
                    val map = tip.asStringKeyMap()
                    val text = map.fitString("text") ?: map.fitString("body") ?: map.fitString("message")
                    val surfaces = map.fitStringList("surfaces").ifEmpty { listOf("today_coach") }
                    text?.let { it to surfaces }
                }
                else -> null
            }
        }
        return parsed.firstOrNull { "today_coach" in it.second }?.first ?: parsed.firstOrNull()?.first
    }

    private fun filterDishes(dishes: List<DishHit>, query: String): List<DishHit> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return dishes
        return dishes.filter { dish ->
            dish.nameEn.lowercase().contains(q) ||
                dish.detail.lowercase().contains(q) ||
                dish.tags.any { it.lowercase().contains(q) }
        }
    }
}

private fun Exception.fitMessage(): String {
    val functions = this as? FirebaseFunctionsException
    val code = functions?.code?.name
    val text = message?.takeIf { it.isNotBlank() }
    return listOfNotNull(code?.lowercase()?.replace('_', ' '), text).distinct().joinToString(": ")
        .ifBlank { "Something went wrong. Try again." }
}
