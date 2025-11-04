package com.fakhry.pomodojo.preferences.data.repository

import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.AppTheme
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PreferencesRepository(
    private val storage: PreferenceStorage,
    private val cascadeResolver: PreferenceCascadeResolver,
    private val validator: PreferencesValidator = PreferencesValidator,
) {

    val preferences: Flow<PomodoroPreferences> = storage.preferences
        .distinctUntilChanged()

    suspend fun updateRepeatCount(value: Int) {
        require(validator.isValidRepeatCount(value)) {
            "Invalid repeat count: $value"
        }
        storage.update { it.copy(repeatCount = value) }
    }

    suspend fun updateFocusMinutes(value: Int) {
        require(validator.isValidFocusMinutes(value)) {
            "Invalid focus minutes: $value"
        }
        val cascade = cascadeResolver.resolveForFocus(value)
        storage.update {
            it.copy(
                focusMinutes = value,
                breakMinutes = cascade.breakMinutes,
                longBreakAfter = cascade.longBreakAfterCount,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    suspend fun updateBreakMinutes(value: Int) {
        require(validator.isValidBreakMinutes(value)) {
            "Invalid break minutes: $value"
        }
        val cascade = cascadeResolver.resolveForBreak(value)
        storage.update {
            it.copy(
                breakMinutes = value,
                longBreakAfter = cascade.longBreakAfterCount,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    suspend fun updateLongBreakEnabled(enabled: Boolean) {
        storage.update { it.copy(longBreakEnabled = enabled) }
    }

    suspend fun updateLongBreakAfter(value: Int) {
        require(validator.isValidLongBreakAfter(value)) {
            "Invalid long break after count: $value"
        }
        storage.update { it.copy(longBreakAfter = value) }
    }

    suspend fun updateLongBreakMinutes(value: Int) {
        require(validator.isValidLongBreakMinutes(value)) {
            "Invalid long break minutes: $value"
        }
        storage.update { it.copy(longBreakMinutes = value) }
    }

    suspend fun updateAppTheme(theme: AppTheme) {
        storage.update { it.copy(appTheme = theme) }
    }
}
