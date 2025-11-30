package com.fakhry.pomodojo.data.preferences.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.flow.distinctUntilChanged

class PreferencesRepositoryImpl(
    private val storage: PreferenceStorage,
    private val cascadeResolver: PreferenceCascadeResolver,
) : PreferencesRepository {
    override val preferences = storage.preferences.distinctUntilChanged()

    override suspend fun updateRepeatCount(value: Int) {
        storage.updatePreferences { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForFocus(value)
        storage.updatePreferences {
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
        storage.updatePreferences {
            it.copy(
                breakMinutes = value,
                longBreakAfter = cascade.longBreakAfterCount,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        storage.updatePreferences { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        storage.updatePreferences { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        storage.updatePreferences { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        storage.updatePreferences { it.copy(alwaysOnDisplayEnabled = enabled) }
    }
}
