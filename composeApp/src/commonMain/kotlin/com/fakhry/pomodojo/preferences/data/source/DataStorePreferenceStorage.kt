package com.fakhry.pomodojo.preferences.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

const val PREFERENCES_FILE_NAME = "pomodojo.preferences_pb"

internal expect fun provideDataStore(): DataStore<Preferences>

class DataStorePreferenceStorage(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStorage {

    override val preferences: Flow<PreferencesDomain> = dataStore.data
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
        val repeatCount = this[PreferenceKeys.REPEAT_COUNT]
            ?.takeIf(PreferencesValidator::isValidRepeatCount)
            ?: PreferencesDomain.DEFAULT_REPEAT_COUNT

        val focusMinutes = this[PreferenceKeys.FOCUS_MINUTES]
            ?.takeIf(PreferencesValidator::isValidFocusMinutes)
            ?: PreferencesDomain.DEFAULT_FOCUS_MINUTES

        val breakMinutes = this[PreferenceKeys.BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidBreakMinutes)
            ?: PreferencesDomain.DEFAULT_BREAK_MINUTES

        val longBreakEnabled = this[PreferenceKeys.LONG_BREAK_ENABLED] ?: true

        val longBreakAfter = this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
            ?.takeIf(PreferencesValidator::isValidLongBreakAfter)
            ?: PreferencesDomain.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes = this[PreferenceKeys.LONG_BREAK_MINUTES]
            ?.takeIf(PreferencesValidator::isValidLongBreakMinutes)
            ?: PreferencesDomain.DEFAULT_LONG_BREAK_MINUTES

        val appTheme = AppTheme.fromStorage(this[PreferenceKeys.APP_THEME])

        return PreferencesDomain(
            appTheme = appTheme,
            repeatCount = repeatCount,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            longBreakEnabled = longBreakEnabled,
            longBreakAfter = longBreakAfter,
            longBreakMinutes = longBreakMinutes,
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
}
