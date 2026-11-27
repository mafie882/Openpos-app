package com.openpos.app.data

import android.content.Context

/**
 * A handful of local app preferences — plain SharedPreferences, never
 * synced anywhere. Staff PIN lock is off by default: most small shops
 * running this app are a single person and don't need it.
 */
object SettingsPrefs {
    private const val PREFS_NAME = "openpos_settings"
    private const val KEY_REQUIRE_STAFF_PIN = "require_staff_pin"

    fun requireStaffPin(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REQUIRE_STAFF_PIN, false)

    fun setRequireStaffPin(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_REQUIRE_STAFF_PIN, value).apply()
    }
}
