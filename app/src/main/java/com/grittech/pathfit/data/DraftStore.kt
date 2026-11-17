package com.grittech.pathfit.data

import android.content.Context
import com.grittech.pathfit.domain.PlaceResolver
import com.grittech.pathfit.model.OnboardingDraft
import org.json.JSONArray
import org.json.JSONObject

class DraftStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("fitpath_drafts", Context.MODE_PRIVATE)

    fun load(uid: String): OnboardingDraft? {
        val raw = prefs.getString(key(uid), null) ?: return null
        return runCatching { decode(JSONObject(raw)) }.getOrNull()
    }

    fun save(uid: String, draft: OnboardingDraft) {
        prefs.edit().putString(key(uid), encode(draft).toString()).apply()
    }

    private fun key(uid: String) = "draft_$uid"

    private fun encode(draft: OnboardingDraft) = JSONObject().apply {
        put("onboardingStep", draft.onboardingStep)
        put("displayName", draft.displayName)
        put("countryCode", draft.countryCode)
        if (draft.regionKey == null) put("regionKey", JSONObject.NULL) else put("regionKey", draft.regionKey)
        put("locationLabel", draft.locationLabel)
        put("dateOfBirth", draft.dateOfBirth)
        put("sexAtBirth", draft.sexAtBirth)
        put("heightCm", draft.heightCm)
        put("heightUnit", draft.heightUnit)
        put("weightKg", draft.weightKg)
        put("targetWeightKg", draft.targetWeightKg)
        put("activityLevel", draft.activityLevel)
        put("goal", draft.goal)
        put("weeklyPaceKg", draft.weeklyPaceKg)
        put("allergenFlags", JSONArray(draft.allergenFlags))
        put("dietTypes", JSONArray(draft.dietTypes))
        put("conditionKeys", JSONArray(draft.conditionKeys))
        put("timezone", draft.timezone)
        put("schemaVersion", draft.schemaVersion)
    }

    private fun decode(json: JSONObject) = OnboardingDraft(
        onboardingStep = json.optString("onboardingStep", "about_you"),
        displayName = json.optString("displayName"),
        countryCode = json.optString("countryCode", "PK"),
        regionKey = when {
            !json.has("regionKey") || json.isNull("regionKey") -> null
            else -> PlaceResolver.migrateRegionKey(json.optString("regionKey").ifBlank { null })
        },
        locationLabel = json.optString("locationLabel", "Lahore, Punjab"),
        dateOfBirth = json.optString("dateOfBirth", "1998-05-14"),
        sexAtBirth = json.optString("sexAtBirth", "male"),
        heightCm = json.optInt("heightCm", 178),
        heightUnit = json.optString("heightUnit", "cm"),
        weightKg = json.optDouble("weightKg", 78.5),
        targetWeightKg = json.optDouble("targetWeightKg", 72.0),
        activityLevel = json.optString("activityLevel", "moderate"),
        goal = json.optString("goal", "lose"),
        weeklyPaceKg = json.optDouble("weeklyPaceKg", 0.5),
        allergenFlags = json.optJSONArray("allergenFlags").toStrings(),
        dietTypes = json.optJSONArray("dietTypes").toStrings(),
        conditionKeys = json.optJSONArray("conditionKeys").toStrings(),
        timezone = json.optString("timezone", "Asia/Karachi"),
        schemaVersion = json.optInt("schemaVersion", 1),
    )

    private fun JSONArray?.toStrings(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }
}
