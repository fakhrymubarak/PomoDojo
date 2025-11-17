package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePreferencesRepository(initial: PreferencesDomain) : PreferencesRepository {
    private val state = MutableStateFlow(initial)
    val current: PreferencesDomain get() = state.value

    override val preferences: Flow<PreferencesDomain> = state.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        state.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        state.update { it.copy(focusMinutes = value) }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        state.update { it.copy(breakMinutes = value) }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        state.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        state.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        state.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAppTheme(theme: AppTheme) {
        state.update { it.copy(appTheme = theme) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        state.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        state.update { it.copy(hasActiveSession = value) }
    }

    fun emit(preferences: PreferencesDomain) {
        state.value = preferences
    }
}
