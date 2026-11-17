package com.grittech.pathfit.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.grittech.pathfit.data.FitPathRepository
import com.grittech.pathfit.data.Outcome
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.DishSearch
import com.grittech.pathfit.domain.PaceLogic
import com.grittech.pathfit.domain.PlaceResolver
import com.grittech.pathfit.model.CommonCard
import com.grittech.pathfit.model.CustomMealDraft
import com.grittech.pathfit.model.DishHit
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.model.IngredientHit
import com.grittech.pathfit.model.MealLog
import com.grittech.pathfit.model.OnboardingDraft
import com.grittech.pathfit.model.PickerMode
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.model.RecipeDraft
import com.grittech.pathfit.model.RecipeLine
import com.grittech.pathfit.model.Targets
import com.grittech.pathfit.model.UsualItem
import com.grittech.pathfit.model.UsualMeal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone
import java.util.UUID

sealed interface NavCommand {
    data class To(val route: String, val clear: Boolean = false) : NavCommand
}

object Routes {
    const val Splash = "splash"
    const val SignIn = "sign_in"
    const val About = "about"
    const val Biometrics = "biometrics"
    const val Activity = "activity"
    const val Pace = "pace"
    const val Diet = "diet"
    const val Preview = "preview"
    const val Staples = "staples"
    const val BuildMeal = "build_meal"
    const val Dishes = "dishes"
    const val Ingredients = "ingredients"
    const val Recipe = "recipe"
    const val Home = "home"
}

data class FitUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val agreed: Boolean = true,
    val splashError: String? = null,
    val draft: OnboardingDraft = OnboardingDraft(),
    val targets: Targets? = null,
    val entitlement: String = "none",
    val onboardingStatus: String = "pending",
    val dayKey: String = "",
    val selectedSlot: String = "lunch",
    val logs: List<MealLog> = emptyList(),
    val waterMl: Int = 0,
    val usuals: List<UsualMeal> = emptyList(),
    val staples: Map<String, List<CommonCard>> = emptyMap(),
    val staplesOffline: Boolean = false,
    val selectedTemplates: Set<String> = emptySet(),
    val custom: CustomMealDraft = CustomMealDraft(),
    val recipe: RecipeDraft = RecipeDraft(),
    val dishes: List<DishHit> = emptyList(),
    val dishQuery: String = "",
    val dishTag: String? = null,
    val selectedDishId: String? = null,
    val dishQty: Int = 1,
    val dishPrep: String? = null,
    val ingredients: List<IngredientHit> = emptyList(),
    val ingredientQuery: String = "",
    val ingredientCategory: String = "all",
    val ingredientGrams: Int = 100,
    val selectedIngredientId: String? = null,
    val homeTab: HomeTab = HomeTab.Today,
    val pickerMode: PickerMode = PickerMode.AddToPlate,
    val snackbar: String? = null,
    val undoIds: List<String> = emptyList(),
    val showAdjust: Boolean = false,
    val adjustItems: List<UsualItem> = emptyList(),
    val coachTip: String? = null,
    val showBmrInfo: Boolean = false,
    val offline: Boolean = false,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FitPathRepository(app)
    private val _state = MutableStateFlow(FitUiState())
    val state = _state.asStateFlow()
    private val _nav = MutableSharedFlow<NavCommand>(extraBufferCapacity = 8)
    val nav = _nav.asSharedFlow()

    private var targetsReg: ListenerRegistration? = null
    private var insightsReg: ListenerRegistration? = null
    private var usualsReg: ListenerRegistration? = null
    private var logsReg: ListenerRegistration? = null
    private var drinksReg: ListenerRegistration? = null
    private var boundDay: String? = null
    private var dishSearchJob: Job? = null

    init {
        repo.currentUid()?.let { attach(it, navigate = false) }
    }

    fun clearError() = _state.update { it.copy(error = null, snackbar = null) }

    fun setAgreed(value: Boolean) = _state.update { it.copy(agreed = value) }

    fun onSplashFinished() {
        viewModelScope.launch {
            val uid = repo.currentUid()
            if (uid == null) {
                go(Routes.SignIn, clear = true)
                return@launch
            }
            when (val boot = repo.bootstrap()) {
                is Outcome.Err -> _state.update { it.copy(splashError = boot.message) }
                is Outcome.Ok -> {
                    _state.update {
                        it.copy(
                            splashError = null,
                            onboardingStatus = boot.value.onboardingStatus,
                            entitlement = boot.value.subscriptionStatus,
                        )
                    }
                    attach(uid, navigate = false)
                    routeAfterAuth(boot.value.onboardingStatus)
                }
            }
        }
    }

    fun signIn(activity: android.app.Activity) {
        if (!_state.value.agreed) {
            _state.update { it.copy(error = "Agree to the Terms of Service and Privacy Policy.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val signed = repo.signInWithGoogle(activity)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = signed.message) }
                is Outcome.Ok -> when (val boot = repo.bootstrap()) {
                    is Outcome.Err -> _state.update { it.copy(busy = false, error = boot.message) }
                    is Outcome.Ok -> {
                        val uid = repo.currentUid()
                        _state.update {
                            it.copy(
                                busy = false,
                                onboardingStatus = boot.value.onboardingStatus,
                                entitlement = boot.value.subscriptionStatus,
                            )
                        }
                        if (uid != null) attach(uid, navigate = false)
                        routeAfterAuth(boot.value.onboardingStatus)
                    }
                }
            }
        }
    }

    fun signOut() {
        go(Routes.SignIn, clear = true)
        clearListeners()
        repo.signOut()
        _state.value = FitUiState()
    }

    fun deleteAccount(activity: android.app.Activity) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = repo.deleteAccount(activity)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    go(Routes.SignIn, clear = true)
                    clearListeners()
                    _state.value = FitUiState(busy = false)
                }
            }
        }
    }

    fun editDraft(block: (OnboardingDraft) -> OnboardingDraft) {
        val next = block(_state.value.draft)
        _state.update { it.copy(draft = next) }
        repo.currentUid()?.let { repo.drafts.save(it, next) }
    }

    fun markStep(step: String) = editDraft { it.copy(onboardingStep = step) }

    fun openOnboarding() {
        repo.session.forceOnboarding = true
        editDraft { it.copy(onboardingStep = "about_you") }
        go(Routes.About, clear = true)
    }

    fun useDashboard() {
        repo.session.forceOnboarding = false
        go(Routes.Home, clear = true)
    }

    fun setGoal(goal: String) {
        editDraft { it.copy(goal = goal, weeklyPaceKg = PaceLogic.defaultPace(goal)) }
    }

    fun calculateTargets() {
        val draft = _state.value.draft
        val problem = validate(draft)
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = repo.completeOnboarding(draft.copy(onboardingStep = "preview"))) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    repo.session.forceOnboarding = false
                    editDraft { it.copy(onboardingStep = "preview") }
                    _state.update {
                        it.copy(
                            busy = false,
                            targets = result.value,
                            onboardingStatus = "complete",
                            entitlement = "trialing",
                        )
                    }
                    go(Routes.Preview, clear = true)
                }
            }
        }
    }

    fun openMealSetup() {
        go(Routes.Staples)
        loadStaples()
    }

    fun loadStaples() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            repo.logRegionContext("loadStaples")
            when (val result = repo.suggestAll()) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    val offline = result.value.values.any { cards -> cards.any { it.source == "offline" } }
                    logStaplesGotVsShown("staplesScreen", result.value, showingLimit = null)
                    _state.update { it.copy(busy = false, staples = result.value, staplesOffline = offline || !repo.isOnline()) }
                }
            }
        }
    }

    fun toggleTemplate(id: String) {
        _state.update { state ->
            val next = state.selectedTemplates.toMutableSet()
            if (!next.add(id)) next.remove(id)
            state.copy(selectedTemplates = next)
        }
    }

    fun saveStaples() {
        val selected = _state.value.staples.values.flatten()
            .filter { it.templateId in _state.value.selectedTemplates }
        if (selected.isEmpty()) {
            go(Routes.Home, clear = true)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = repo.saveUsualFromCards(selected)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    repo.session.forceOnboarding = false
                    _state.update { it.copy(busy = false, snackbar = "Saved ${result.value} usuals") }
                    go(Routes.Home, clear = true)
                }
            }
        }
    }

    fun skipToToday() {
        repo.session.forceOnboarding = false
        go(Routes.Home, clear = true)
    }

    fun startCustom(slot: String) {
        val name = when (slot) {
            "breakfast" -> "Custom Nashta Plate"
            "lunch" -> "Custom Lunch Plate"
            "dinner" -> "Custom Dinner Plate"
            else -> "Custom Snack Plate"
        }
        _state.update { it.copy(custom = CustomMealDraft(mealSlot = slot, name = name, items = emptyList())) }
        go(Routes.BuildMeal)
    }

    fun editCustom(block: (CustomMealDraft) -> CustomMealDraft) {
        _state.update { it.copy(custom = block(it.custom)) }
    }

    fun openDishPicker(mode: PickerMode) {
        _state.update { it.copy(pickerMode = mode, dishQuery = "", dishTag = null, selectedDishId = null, dishQty = 1) }
        go(Routes.Dishes)
        searchDishes("")
    }

    fun searchDishes(query: String, tag: String? = _state.value.dishTag) {
        _state.update { it.copy(dishQuery = query, dishTag = tag) }
        dishSearchJob?.cancel()
        dishSearchJob = viewModelScope.launch {
            // Wait while the user is still typing; empty query / tag-only runs immediately.
            if (query.isNotBlank()) delay(DISH_SEARCH_DEBOUNCE_MS)
            val searchText = when {
                tag != null && query.isNotBlank() -> query
                tag != null -> tag
                else -> query
            }
            val hits = repo.searchDishes(searchText)
            val filtered = when {
                tag != null && query.isNotBlank() ->
                    hits.filter { DishSearch.matches(it, query) && DishSearch.matches(it, tag) }
                else -> hits
            }
            Log.d(
                TAG,
                "dishes got=${hits.size} showing=${filtered.size} query='$query' tag=$tag " +
                    "gotNames=${hits.map { it.nameEn }} shownNames=${filtered.map { it.nameEn }}",
            )
            _state.update {
                it.copy(
                    dishes = filtered,
                    selectedDishId = it.selectedDishId ?: filtered.firstOrNull()?.dishId,
                    dishPrep = filtered.firstOrNull()?.preps?.firstOrNull(),
                )
            }
        }
    }

    fun selectDish(id: String) {
        val dish = _state.value.dishes.firstOrNull { it.dishId == id }
        _state.update { it.copy(selectedDishId = id, dishPrep = dish?.preps?.firstOrNull(), dishQty = 1) }
    }

    fun setDishQty(qty: Int) = _state.update { it.copy(dishQty = qty.coerceIn(1, 12)) }

    fun setDishPrep(prep: String) = _state.update { it.copy(dishPrep = prep) }

    fun confirmDish() {
        val state = _state.value
        val dish = state.dishes.firstOrNull { it.dishId == state.selectedDishId } ?: return
        val portion = state.dishQty.toDouble()
        if (state.pickerMode == PickerMode.LogOnce) {
            viewModelScope.launch {
                val item = dish.toUsualItem(portion, state.dishPrep)
                when (val result = repo.logItems(state.dayKey, state.selectedSlot, dish.nameEn, listOf(item), null)) {
                    is Outcome.Err -> _state.update { it.copy(error = result.message) }
                    is Outcome.Ok -> {
                        _state.update { it.copy(undoIds = result.value, snackbar = logMessage("Logged ${dish.nameEn}")) }
                        go(Routes.Home)
                    }
                }
            }
            return
        }
        val plate = dish.toPlate(portion, state.dishPrep)
        _state.update { it.copy(custom = it.custom.copy(items = it.custom.items + plate)) }
        go(Routes.BuildMeal)
    }

    fun saveCustom(logToday: Boolean) {
        val custom = _state.value.custom
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = repo.saveCustomUsual(custom.name, custom.mealSlot, custom.items, logToday, _state.value.dayKey)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    val note = if (logToday) "Saved and logged" else "Saved to Your Usuals"
                    _state.update { it.copy(busy = false, snackbar = logMessage(note)) }
                    // Prefer Don't Log on first Meal Setup — return to staples hub; Log → Today
                    if (logToday) {
                        repo.session.forceOnboarding = false
                        go(Routes.Home, clear = true)
                    } else {
                        go(Routes.Staples)
                    }
                }
            }
        }
    }

    fun openRecipe() {
        _state.update { it.copy(recipe = RecipeDraft(name = "", mealSlot = _state.value.custom.mealSlot)) }
        go(Routes.Recipe)
    }

    fun editRecipe(block: (RecipeDraft) -> RecipeDraft) = _state.update { it.copy(recipe = block(it.recipe)) }

    fun openIngredients() {
        _state.update { it.copy(pickerMode = PickerMode.AddToRecipe) }
        go(Routes.Ingredients)
        searchIngredients(_state.value.ingredientQuery, _state.value.ingredientCategory)
    }

    fun searchIngredients(query: String, category: String) {
        _state.update { it.copy(ingredientQuery = query, ingredientCategory = category) }
        viewModelScope.launch {
            val hits = repo.searchIngredients(query, category)
            _state.update {
                it.copy(
                    ingredients = hits,
                    selectedIngredientId = it.selectedIngredientId ?: hits.firstOrNull()?.id,
                )
            }
        }
    }

    fun selectIngredient(id: String) = _state.update { it.copy(selectedIngredientId = id) }

    fun setIngredientGrams(grams: Int) = _state.update { it.copy(ingredientGrams = grams.coerceIn(1, 2000)) }

    fun confirmIngredient() {
        val state = _state.value
        val hit = state.ingredients.firstOrNull { it.id == state.selectedIngredientId } ?: return
        val factor = state.ingredientGrams / 100.0
        val line = RecipeLine(
            ingredientId = hit.id,
            name = hit.nameEn,
            grams = state.ingredientGrams.toDouble(),
            kcal = hit.kcalPer100g * factor,
            proteinG = hit.proteinPer100g * factor,
            carbsG = hit.carbsPer100g * factor,
            fatG = hit.fatPer100g * factor,
        )
        _state.update { it.copy(recipe = it.recipe.copy(lines = it.recipe.lines + line)) }
        go(Routes.Recipe)
    }

    fun saveRecipe(addToMeal: Boolean) {
        val recipe = _state.value.recipe
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = repo.saveRecipe(recipe)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> {
                    if (addToMeal) {
                        val kcal = recipe.lines.sumOf { it.kcal }
                        val plate = PlateItem(
                            key = UUID.randomUUID().toString(),
                            userRecipeId = result.value,
                            name = recipe.name,
                            detail = "Custom recipe",
                            portion = 1.0,
                            unit = "serving",
                            kcalPerServing = kcal,
                            proteinPer = recipe.lines.sumOf { it.proteinG },
                            carbsPer = recipe.lines.sumOf { it.carbsG },
                            fatPer = recipe.lines.sumOf { it.fatG },
                        )
                        _state.update { it.copy(busy = false, custom = it.custom.copy(items = it.custom.items + plate)) }
                        go(Routes.BuildMeal)
                    } else {
                        _state.update { it.copy(busy = false, snackbar = "Recipe saved") }
                        go(Routes.BuildMeal)
                    }
                }
            }
        }
    }

    fun selectTab(tab: HomeTab) = _state.update { it.copy(homeTab = tab) }

    fun selectSlot(slot: String) = _state.update { it.copy(selectedSlot = slot, showAdjust = false) }

    fun shiftDay(days: Int) {
        val tz = _state.value.draft.timezone
        val current = DayClock.resolveKey(_state.value.dayKey, tz)
        val next = DayClock.shiftKey(current, days, tz)
        _state.update { it.copy(dayKey = next) }
        repo.currentUid()?.let { bindDay(it, next) }
    }

    fun openAdjust() {
        val usual = heroUsual() ?: return
        _state.update { it.copy(showAdjust = true, adjustItems = usual.items) }
    }

    fun closeAdjust() = _state.update { it.copy(showAdjust = false) }

    fun updateAdjustPortion(index: Int, portion: Double) {
        _state.update { state ->
            val items = state.adjustItems.toMutableList()
            if (index in items.indices) items[index] = items[index].copy(portion = portion.coerceIn(0.5, 12.0))
            state.copy(adjustItems = items)
        }
    }

    fun logUsual(id: String) {
        val usual = _state.value.usuals.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, selectedSlot = usual.mealSlot) }
            when (val result = repo.logItems(_state.value.dayKey, usual.mealSlot, usual.name, usual.items, usual.id)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> _state.update {
                    it.copy(busy = false, undoIds = result.value, snackbar = logMessage("Logged ${usual.name}"))
                }
            }
        }
    }

    fun logHero(adjusted: Boolean) {
        val usual = heroUsual() ?: return
        val items = if (adjusted) _state.value.adjustItems else usual.items
        viewModelScope.launch {
            _state.update { it.copy(busy = true, showAdjust = false) }
            when (val result = repo.logItems(_state.value.dayKey, usual.mealSlot, usual.name, items, usual.id)) {
                is Outcome.Err -> _state.update { it.copy(busy = false, error = result.message) }
                is Outcome.Ok -> _state.update {
                    it.copy(busy = false, undoIds = result.value, snackbar = logMessage("Logged ${usual.name}"))
                }
            }
        }
    }

    fun undoLast() = undoLogIds(_state.value.undoIds)

    fun undoLogIds(ids: List<String>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            runCatching { repo.undoLogs(ids) }
            _state.update { it.copy(undoIds = emptyList(), snackbar = "Undone") }
        }
    }

    fun addGlass() {
        viewModelScope.launch {
            when (val result = repo.addGlass(_state.value.dayKey)) {
                is Outcome.Err -> _state.update { it.copy(error = result.message) }
                is Outcome.Ok -> _state.update { it.copy(snackbar = logMessage("Added 250 ml")) }
            }
        }
    }

    fun logCommonOnce(card: CommonCard) {
        viewModelScope.launch {
            val items = card.items.map { item ->
                UsualItem(
                    dishId = item.dishId,
                    name = item.nameEn,
                    portion = item.defaultPortion,
                    oilLevel = if (item.addedFatKcal > 0) "normal" else null,
                    unit = item.unit,
                    count = item.defaultPortion,
                    kcalPerServing = if (item.defaultPortion == 0.0) item.estimateKcal.toDouble() else item.estimateKcal / item.defaultPortion,
                    proteinPer = item.proteinG,
                    carbsPer = item.carbsG,
                    fatPer = item.fatG,
                    addedFatKcal = item.addedFatKcal,
                )
            }
            when (val result = repo.logItems(_state.value.dayKey, _state.value.selectedSlot, card.nameEn, items, null)) {
                is Outcome.Err -> _state.update { it.copy(error = result.message) }
                is Outcome.Ok -> _state.update { it.copy(undoIds = result.value, snackbar = logMessage("Logged ${card.nameEn}")) }
            }
        }
    }

    fun pinCommon(card: CommonCard) {
        viewModelScope.launch {
            when (val result = repo.saveUsualFromCards(listOf(card.copy(mealSlot = _state.value.selectedSlot)))) {
                is Outcome.Err -> _state.update { it.copy(error = result.message) }
                is Outcome.Ok -> _state.update { it.copy(snackbar = "Added to Your Usuals") }
            }
        }
    }

    fun ensureSlotSuggestions() {
        val slot = _state.value.selectedSlot
        if (_state.value.staples[slot].orEmpty().isNotEmpty()) {
            logStaplesGotVsShown("home/$slot/cached", _state.value.staples, showingLimit = 4)
            return
        }
        viewModelScope.launch {
            when (val result = repo.suggestSlot(slot)) {
                is Outcome.Ok -> {
                    val next = _state.value.staples + (slot to result.value)
                    logStaplesGotVsShown("home/$slot", next, showingLimit = 4)
                    _state.update { state -> state.copy(staples = next) }
                }
                is Outcome.Err -> _state.update { it.copy(error = result.message) }
            }
        }
    }

    fun showBmr(show: Boolean) = _state.update { it.copy(showBmrInfo = show) }

    fun heroUsual(): UsualMeal? {
        val slot = _state.value.selectedSlot
        return _state.value.usuals
            .filter { it.status == "active" && it.mealSlot == slot }
            .sortedWith(compareByDescending<UsualMeal> { it.pinned }.thenByDescending { it.lastUsedAt })
            .firstOrNull()
    }

    private fun attach(uid: String, navigate: Boolean) {
        val existing = repo.drafts.load(uid)
        val deviceTz = TimeZone.getDefault().id.ifBlank { "Asia/Karachi" }
        val base = existing ?: OnboardingDraft(timezone = deviceTz)
        val name = base.displayName.ifBlank { repo.currentDisplayName().orEmpty() }
        val draft = base.copy(
            displayName = name,
            timezone = base.timezone.ifBlank { deviceTz },
            regionKey = PlaceResolver.migrateRegionKey(base.regionKey),
        )
        val day = DayClock.todayKey(draft.timezone)
        repo.logRegionContext("attach")
        Log.d(
            TAG,
            "attach uid=$uid regionKey=${draft.regionKey} countryCode=${draft.countryCode} " +
                "locationLabel=${draft.locationLabel} dayKey=$day",
        )
        _state.update {
            it.copy(
                draft = draft,
                dayKey = it.dayKey.ifBlank { day },
                selectedSlot = DayClock.defaultSlot(DayClock.hour(draft.timezone)),
                offline = !repo.isOnline(),
            )
        }
        repo.drafts.save(uid, draft)
        clearListeners()
        targetsReg = repo.listenTargets(uid) { targets ->
            if (targets != null) _state.update { it.copy(targets = targets) }
        }
        insightsReg = repo.listenInsights(uid) { tip -> _state.update { it.copy(coachTip = tip) } }
        usualsReg = repo.listenUsuals(uid) { meals -> _state.update { it.copy(usuals = meals) } }
        bindDay(uid, _state.value.dayKey.ifBlank { day })
        if (navigate) Unit
    }

    private fun logStaplesGotVsShown(
        surface: String,
        bySlot: Map<String, List<CommonCard>>,
        showingLimit: Int?,
    ) {
        bySlot.forEach { (slot, cards) ->
            val dishesGot = cards.sumOf { it.items.size }
            val shownCards = if (showingLimit == null) cards else cards.take(showingLimit)
            val dishesShown = shownCards.sumOf { it.items.size }
            Log.d(
                TAG,
                "staples [$surface]/$slot gotCards=${cards.size} shownCards=${shownCards.size} " +
                    "gotDishes=$dishesGot shownDishes=$dishesShown " +
                    "got=${cards.map { c -> "${c.nameEn}(${c.items.map { it.nameEn }})" }} " +
                    "shown=${shownCards.map { c -> "${c.nameEn}(${c.items.map { it.nameEn }})" }}",
            )
        }
        val allCards = bySlot.values.flatten()
        val allShown = if (showingLimit == null) {
            allCards
        } else {
            bySlot.values.flatMap { it.take(showingLimit) }
        }
        Log.d(
            TAG,
            "staples [$surface]/ALL gotCards=${allCards.size} shownCards=${allShown.size} " +
                "gotDishes=${allCards.sumOf { it.items.size }} shownDishes=${allShown.sumOf { it.items.size }}",
        )
    }

    private fun bindDay(uid: String, day: String) {
        if (boundDay == day && logsReg != null) return
        boundDay = day
        logsReg?.remove()
        drinksReg?.remove()
        logsReg = repo.listenLogs(uid, day) { logs -> _state.update { it.copy(logs = logs) } }
        drinksReg = repo.listenDrinks(uid, day) { ml -> _state.update { it.copy(waterMl = ml) } }
    }

    private suspend fun routeAfterAuth(status: String) {
        val force = repo.session.forceOnboarding
        val showOnboarding = force || status != "complete"
        if (showOnboarding) {
            if (force) {
                editDraft { draft ->
                    if (draft.onboardingStep == "done" || draft.onboardingStep == "preview") {
                        draft.copy(onboardingStep = "about_you")
                    } else draft
                }
            }
            val step = _state.value.draft.onboardingStep
            val route = when (step) {
                "biometrics" -> Routes.Biometrics
                "activity_goals" -> Routes.Activity
                "target_pace" -> Routes.Pace
                "diet_safeguards" -> Routes.Diet
                "preview" -> if (_state.value.targets?.calorieTarget != null) Routes.Preview else Routes.About
                "done" -> Routes.About
                else -> Routes.About
            }
            go(route, clear = true)
            return
        }
        if (_state.value.targets?.status != "active") {
            when (val refreshed = repo.refreshTargets()) {
                is Outcome.Ok -> _state.update { it.copy(targets = refreshed.value) }
                is Outcome.Err -> Unit
            }
        }
        go(Routes.Home, clear = true)
    }

    private fun stepRoute(step: String): String = when (step) {
        "biometrics" -> Routes.Biometrics
        "activity_goals" -> Routes.Activity
        "target_pace" -> Routes.Pace
        "diet_safeguards" -> Routes.Diet
        "preview" -> Routes.Preview
        "done" -> Routes.Home
        else -> Routes.About
    }

    private fun validate(draft: OnboardingDraft): String? {
        if (draft.displayName.isBlank()) return "Add the name we should use."
        if (draft.locationLabel.isBlank() || draft.countryCode.isBlank()) return "Choose where you are based."
        if (draft.dateOfBirth.length != 10) return "Choose a date of birth."
        if (draft.sexAtBirth != "male" && draft.sexAtBirth != "female") return "Choose biological sex."
        if (draft.heightCm !in 120..230) return "Height must be between 120 and 230 cm."
        if (draft.weightKg !in 30.0..300.0) return "Weight must be between 30 and 300 kg."
        if (draft.targetWeightKg !in 30.0..300.0) return "Target weight must be between 30 and 300 kg."
        if (draft.activityLevel !in setOf("sedentary", "moderate", "active")) return "Choose an activity level."
        if (draft.goal.isBlank()) return "Choose a goal."
        return null
    }

    private fun logMessage(action: String): String =
        if (repo.isOnline()) action else "$action. Will sync when online"

    private fun go(route: String, clear: Boolean = false) {
        _nav.tryEmit(NavCommand.To(route, clear))
    }

    private fun clearListeners() {
        targetsReg?.remove()
        insightsReg?.remove()
        usualsReg?.remove()
        logsReg?.remove()
        drinksReg?.remove()
        targetsReg = null
        insightsReg = null
        usualsReg = null
        logsReg = null
        drinksReg = null
        boundDay = null
    }

    override fun onCleared() {
        clearListeners()
        super.onCleared()
    }

    companion object {
        private const val TAG = "FitPathFirebase"
        private const val DISH_SEARCH_DEBOUNCE_MS = 400L
    }
}

private fun DishHit.toPlate(portion: Double, prep: String?) = PlateItem(
    key = UUID.randomUUID().toString(),
    dishId = dishId,
    name = nameEn,
    detail = prep ?: detail,
    portion = portion,
    unit = unit,
    kcalPerServing = kcal,
    proteinPer = proteinG,
    carbsPer = carbsG,
    fatPer = fatG,
    addedFatKcal = addedFatKcal,
    oilLevel = if (addedFatKcal > 0) "normal" else null,
    prep = prep,
)

private fun DishHit.toUsualItem(portion: Double, prep: String?) = UsualItem(
    dishId = dishId,
    name = nameEn,
    portion = portion,
    oilLevel = if (addedFatKcal > 0) "normal" else null,
    unit = unit,
    count = portion,
    kcalPerServing = kcal,
    proteinPer = proteinG,
    carbsPer = carbsG,
    fatPer = fatG,
    addedFatKcal = addedFatKcal,
)
