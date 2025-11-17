package com.fakhry.pomodojo.features.preferences.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStorePreferenceStorage(private val dataStore: DataStore<Preferences>) :
    PreferenceStorage {
    override val preferences: Flow<PreferencesDomain> =
        dataStore.data
            .map { it.toDomain() }
            .distinctUntilChanged()

    override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
        dataStore.edit { prefs ->
            val current = prefs.toDomain()
            val updated = transform(current)
            prefs.write(updated)
        }
    }

    private fun Preferences.toDomain(): PreferencesDomain {
        val repeatCount =
            this[PreferenceKeys.REPEAT_COUNT] ?: PreferencesDomain.DEFAULT_REPEAT_COUNT

        val focusMinutes =
            this[PreferenceKeys.FOCUS_MINUTES] ?: PreferencesDomain.DEFAULT_FOCUS_MINUTES

        val breakMinutes =
            this[PreferenceKeys.BREAK_MINUTES] ?: PreferencesDomain.DEFAULT_BREAK_MINUTES

        val longBreakEnabled = this[PreferenceKeys.LONG_BREAK_ENABLED] ?: true

        val longBreakAfter =
            this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
                ?: PreferencesDomain.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes =
            this[PreferenceKeys.LONG_BREAK_MINUTES] ?: PreferencesDomain.DEFAULT_LONG_BREAK_MINUTES

        val appTheme = AppTheme.fromStorage(this[PreferenceKeys.APP_THEME])
        val alwaysOnDisplayEnabled = this[PreferenceKeys.ALWAYS_ON_DISPLAY_ENABLED] ?: false
        val hasActiveSession = this[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false

        return PreferencesDomain(
            appTheme = appTheme,
            repeatCount = repeatCount,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            longBreakEnabled = longBreakEnabled,
            longBreakAfter = longBreakAfter,
            longBreakMinutes = longBreakMinutes,
            alwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
            hasActiveSession = hasActiveSession,
        )
    }

    private fun MutablePreferences.write(preferences: PreferencesDomain) {
        this[PreferenceKeys.REPEAT_COUNT] = preferences.repeatCount
        this[PreferenceKeys.FOCUS_MINUTES] = preferences.focusMinutes
        this[PreferenceKeys.BREAK_MINUTES] = preferences.breakMinutes
        this[PreferenceKeys.LONG_BREAK_ENABLED] = preferences.longBreakEnabled
        this[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = preferences.longBreakAfter
        this[PreferenceKeys.LONG_BREAK_MINUTES] = preferences.longBreakMinutes
        this[PreferenceKeys.APP_THEME] = preferences.appTheme.storageValue
        this[PreferenceKeys.ALWAYS_ON_DISPLAY_ENABLED] = preferences.alwaysOnDisplayEnabled
        this[PreferenceKeys.HAS_ACTIVE_SESSION] = preferences.hasActiveSession
    }
}

internal object PreferenceKeys {
    val REPEAT_COUNT = intPreferencesKey("repeat_count")
    val FOCUS_MINUTES = intPreferencesKey("focus_timer_minutes")
    val BREAK_MINUTES = intPreferencesKey("break_timer_minutes")
    val LONG_BREAK_ENABLED = booleanPreferencesKey("long_break_enabled")
    val LONG_BREAK_AFTER_COUNT = intPreferencesKey("long_break_after_count")
    val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
    val APP_THEME = stringPreferencesKey("app_theme")
    val ALWAYS_ON_DISPLAY_ENABLED = booleanPreferencesKey("always_on_display_enabled")
    val HAS_ACTIVE_SESSION = booleanPreferencesKey("has_active_session")
}
