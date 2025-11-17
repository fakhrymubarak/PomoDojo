package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.features.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferencesRepository
import kotlinx.coroutines.flow.distinctUntilChanged

class PreferencesRepositoryImpl(
    private val storage: PreferenceStorage,
    private val cascadeResolver: PreferenceCascadeResolver,
) : PreferencesRepository {
    override val preferences = storage.preferences.distinctUntilChanged()

    override suspend fun updateRepeatCount(value: Int) {
        storage.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
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

    override suspend fun updateBreakMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForBreak(value)
        storage.update {
            it.copy(
                breakMinutes = value,
                longBreakAfter = cascade.longBreakAfterCount,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        storage.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        storage.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        storage.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAppTheme(theme: AppTheme) {
        storage.update { it.copy(appTheme = theme) }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        storage.update { it.copy(hasActiveSession = value) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        storage.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }
}
