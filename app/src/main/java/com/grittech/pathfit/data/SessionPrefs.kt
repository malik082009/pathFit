package com.grittech.pathfit.data

import android.content.Context

/**
 * Controls whether Splash should open onboarding even when Firebase says complete.
 * Defaults to true so the Stitch questionnaire is reachable while building/testing.
 * Cleared after a successful Calculate Targets.
 */
class SessionPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("fitpath_session", Context.MODE_PRIVATE)

    var forceOnboarding: Boolean
        get() = prefs.getBoolean(KEY_FORCE_ONBOARDING, true)
        set(value) = prefs.edit().putBoolean(KEY_FORCE_ONBOARDING, value).apply()

    companion object {
        private const val KEY_FORCE_ONBOARDING = "force_onboarding"
    }
}
