package com.fakhry.pomodojo.features.preferences.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStorePreferenceStorage(private val dataStore: DataStore<Preferences>) :
    PreferenceStorage {
    override val preferences: Flow<PomodoroPreferences> =
        dataStore.data
            .map { it.toPomodoroPreferences() }
            .distinctUntilChanged()

    override val initPreferences: Flow<InitAppPreferences> =
        dataStore.data
            .map { it.toInitPreferences() }
            .distinctUntilChanged()

    override suspend fun updatePreferences(
        transform: (PomodoroPreferences) -> PomodoroPreferences,
    ) {
        dataStore.edit { prefs ->
            val current = prefs.toPomodoroPreferences()
            val updated = transform(current)
            prefs.write(updated)
        }
    }

    override suspend fun updateInitPreferences(
        transform: (InitAppPreferences) -> InitAppPreferences,
    ) {
        dataStore.edit { prefs ->
            val current = prefs.toInitPreferences()
            val updated = transform(current)
            prefs.write(updated)
        }
    }

    private fun Preferences.toPomodoroPreferences(): PomodoroPreferences {
        val repeatCount =
            this[PreferenceKeys.REPEAT_COUNT] ?: PomodoroPreferences.DEFAULT_REPEAT_COUNT

        val focusMinutes =
            this[PreferenceKeys.FOCUS_MINUTES] ?: PomodoroPreferences.DEFAULT_FOCUS_MINUTES

        val breakMinutes =
            this[PreferenceKeys.BREAK_MINUTES] ?: PomodoroPreferences.DEFAULT_BREAK_MINUTES

        val longBreakEnabled = this[PreferenceKeys.LONG_BREAK_ENABLED] ?: true

        val longBreakAfter =
            this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
                ?: PomodoroPreferences.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes =
            this[PreferenceKeys.LONG_BREAK_MINUTES]
                ?: PomodoroPreferences.DEFAULT_LONG_BREAK_MINUTES

        val alwaysOnDisplayEnabled = this[PreferenceKeys.ALWAYS_ON_DISPLAY_ENABLED] ?: false

        return PomodoroPreferences(
            repeatCount = repeatCount,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            longBreakEnabled = longBreakEnabled,
            longBreakAfter = longBreakAfter,
            longBreakMinutes = longBreakMinutes,
            alwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
        )
    }

    private fun Preferences.toInitPreferences(): InitAppPreferences = InitAppPreferences(
        appTheme = AppTheme.fromStorage(this[PreferenceKeys.APP_THEME]),
        hasActiveSession = this[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
    )

    private fun MutablePreferences.write(preferences: PomodoroPreferences) {
        this[PreferenceKeys.REPEAT_COUNT] = preferences.repeatCount
        this[PreferenceKeys.FOCUS_MINUTES] = preferences.focusMinutes
        this[PreferenceKeys.BREAK_MINUTES] = preferences.breakMinutes
        this[PreferenceKeys.LONG_BREAK_ENABLED] = preferences.longBreakEnabled
        this[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = preferences.longBreakAfter
        this[PreferenceKeys.LONG_BREAK_MINUTES] = preferences.longBreakMinutes
        this[PreferenceKeys.ALWAYS_ON_DISPLAY_ENABLED] = preferences.alwaysOnDisplayEnabled
    }

    private fun MutablePreferences.write(preferences: InitAppPreferences) {
        this[PreferenceKeys.APP_THEME] = preferences.appTheme.storageValue
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
