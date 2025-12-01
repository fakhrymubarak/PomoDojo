package com.fakhry.pomodojo.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val REPEAT_COUNT = intPreferencesKey("repeat_count")
    val FOCUS_MINUTES = intPreferencesKey("focus_timer_minutes")
    val BREAK_MINUTES = intPreferencesKey("break_timer_minutes")
    val LONG_BREAK_ENABLED = booleanPreferencesKey("long_break_enabled")
    val LONG_BREAK_AFTER_COUNT = intPreferencesKey("long_break_after_count")
    val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
    val ACTIVE_SESSION_KEY = stringPreferencesKey("active_session_snapshot")

    val APP_THEME = stringPreferencesKey("app_theme")
    val HAS_ACTIVE_SESSION = booleanPreferencesKey("has_active_session")

    val ALWAYS_ON_DISPLAY_ENABLED = booleanPreferencesKey("always_on_display_enabled")
}
