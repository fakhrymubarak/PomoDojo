package com.fakhry.pomodojo.preferences.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

const val PREFERENCES_FILE_NAME = "pomodojo.preferences_pb"

internal expect fun provideDataStore(): DataStore<Preferences>

class DataStorePreferenceStorage(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStorage {

    override val preferences: Flow<PomodoroPreferences> = dataStore.data
        .map { it.toDomain() }
        .distinctUntilChanged()

    override suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences) {
        dataStore.edit { prefs ->
            val current = prefs.toDomain()
            val updated = transform(current)
            prefs.write(updated)
        }
    }

    private fun Preferences.toDomain(): PomodoroPreferences {
        val repeatCount = this[PreferenceKeys.REPEAT_COUNT]
            ?.takeIf(PreferencesValidator::isValidRepeatCount)
            ?: PomodoroPreferences.Companion.DEFAULT_REPEAT_COUNT

        val focusMinutes = this[PreferenceKeys.FOCUS_MINUTES]
            ?.takeIf(PreferencesValidator::isValidFocusMinutes)
            ?: PomodoroPreferences.Companion.DEFAULT_FOCUS_MINUTES

        val breakMinutes = this[PreferenceKeys.BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidBreakMinutes)
            ?: PomodoroPreferences.Companion.DEFAULT_BREAK_MINUTES

        val longBreakEnabled = this[PreferenceKeys.LONG_BREAK_ENABLED] ?: true

        val longBreakAfter = this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
            ?.takeIf(PreferencesValidator::isValidLongBreakAfter)
            ?: PomodoroPreferences.Companion.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes = this[PreferenceKeys.LONG_BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidLongBreakMinutes)
            ?: PomodoroPreferences.Companion.DEFAULT_LONG_BREAK_MINUTES

        return PomodoroPreferences(
            repeatCount = repeatCount,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            longBreakEnabled = longBreakEnabled,
            longBreakAfter = longBreakAfter,
            longBreakMinutes = longBreakMinutes,
        )
    }

    private fun MutablePreferences.write(preferences: PomodoroPreferences) {
        this[PreferenceKeys.REPEAT_COUNT] = preferences.repeatCount
        this[PreferenceKeys.FOCUS_MINUTES] = preferences.focusMinutes
        this[PreferenceKeys.BREAK_MINUTES] = preferences.breakMinutes
        this[PreferenceKeys.LONG_BREAK_ENABLED] = preferences.longBreakEnabled
        this[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = preferences.longBreakAfter
        this[PreferenceKeys.LONG_BREAK_MINUTES] = preferences.longBreakMinutes
    }
}

object PreferenceKeys {
    val REPEAT_COUNT = intPreferencesKey("repeat_count")
    val FOCUS_MINUTES = intPreferencesKey("focus_timer_minutes")
    val BREAK_MINUTES = intPreferencesKey("break_timer_minutes")
    val LONG_BREAK_ENABLED = booleanPreferencesKey("long_break_enabled")
    val LONG_BREAK_AFTER_COUNT = intPreferencesKey("long_break_after_count")
    val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
}