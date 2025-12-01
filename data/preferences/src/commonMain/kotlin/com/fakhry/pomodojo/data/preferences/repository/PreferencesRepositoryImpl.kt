package com.fakhry.pomodojo.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val cascadeResolver: PreferenceCascadeResolver,
) : PreferencesRepository {
    override val preferences = dataStore.data.map {
        it.toPomodoroPreferences()
    }.distinctUntilChanged()

    override suspend fun updateRepeatCount(value: Int) {
        dataStore.edit {
            it[PreferenceKeys.REPEAT_COUNT] = value
        }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForFocus(value)
        dataStore.edit {
            it[PreferenceKeys.FOCUS_MINUTES] = value
            it[PreferenceKeys.BREAK_MINUTES] = cascade.breakMinutes
            it[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = cascade.longBreakAfterCount
            it[PreferenceKeys.LONG_BREAK_MINUTES] = cascade.longBreakMinutes
        }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForBreak(value)
        dataStore.edit {
            it[PreferenceKeys.BREAK_MINUTES] = value
            it[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = cascade.longBreakAfterCount
            it[PreferenceKeys.LONG_BREAK_MINUTES] = cascade.longBreakMinutes
        }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        dataStore.edit {
            it[PreferenceKeys.LONG_BREAK_ENABLED] = enabled
        }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        dataStore.edit {
            it[PreferenceKeys.LONG_BREAK_AFTER_COUNT] = value
        }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        dataStore.edit {
            it[PreferenceKeys.LONG_BREAK_MINUTES] = value
        }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        dataStore.edit {
            it[PreferenceKeys.ALWAYS_ON_DISPLAY_ENABLED] = enabled
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

        val longBreakAfter = this[PreferenceKeys.LONG_BREAK_AFTER_COUNT]
            ?: PomodoroPreferences.DEFAULT_LONG_BREAK_AFTER

        val longBreakMinutes = this[PreferenceKeys.LONG_BREAK_MINUTES]
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

}
