package com.fakhry.pomodojo.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val PREFERENCES_FILE_NAME = "pomodojo.preferences_pb"

class DataStorePreferenceStorage(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStorage {

    override val data: Flow<PomodoroPreferences> = dataStore.data
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
            ?: PomodoroPreferences.DEFAULT_REPEAT_COUNT

        val focusMinutes = this[PreferenceKeys.FOCUS_MINUTES]
            ?.takeIf(PreferencesValidator::isValidFocusMinutes)
            ?: PomodoroPreferences.DEFAULT_FOCUS_MINUTES

        val breakMinutes = this[PreferenceKeys.BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidBreakMinutes)
            ?: PomodoroPreferences.DEFAULT_BREAK_MINUTES

        val longBreakEnabled = this[PreferenceKeys.LONG_BREAK_ENABLED] ?: true

        val longBreakAfter = this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
            ?.takeIf(PreferencesValidator::isValidLongBreakAfter)
            ?: PomodoroPreferences.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes = this[PreferenceKeys.LONG_BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidLongBreakMinutes)
            ?: PomodoroPreferences.DEFAULT_LONG_BREAK_MINUTES

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

internal expect fun provideDataStore(): DataStore<Preferences>

fun createPreferenceStorage(): PreferenceStorage =
    DataStorePreferenceStorage(provideDataStore())

internal fun preferencesFileName(): String = PREFERENCES_FILE_NAME
