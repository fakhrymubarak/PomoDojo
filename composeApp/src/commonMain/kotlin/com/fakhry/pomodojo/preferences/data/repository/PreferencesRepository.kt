package com.fakhry.pomodojo.preferences.data.repository

import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PreferencesRepository(
    private val storage: PreferenceStorage,
    private val cascadeResolver: PreferenceCascadeResolver,
) {

    val preferences: Flow<PreferencesDomain> = storage.preferences
        .distinctUntilChanged()

    suspend fun updateRepeatCount(value: Int) {
        storage.update { it.copy(repeatCount = value) }
    }

    suspend fun updateFocusMinutes(value: Int) {
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
        storage.update { it.copy(longBreakAfter = value) }
    }

    suspend fun updateLongBreakMinutes(value: Int) {
        storage.update { it.copy(longBreakMinutes = value) }
    }

    suspend fun updateAppTheme(theme: AppTheme) {
        storage.update { it.copy(appTheme = theme) }
    }
}
