package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePreferencesRepository(
    initial: PomodoroPreferences = PomodoroPreferences(),
    initialInit: InitAppPreferences = InitAppPreferences(),
) : PreferencesRepository, InitPreferencesRepository {
    private val pomodoroState = MutableStateFlow(initial)
    private val initState = MutableStateFlow(initialInit)
    val current: PomodoroPreferences get() = pomodoroState.value

    override val preferences: Flow<PomodoroPreferences> = pomodoroState.asStateFlow()
    override val initPreferences: Flow<InitAppPreferences> = initState.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        pomodoroState.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        pomodoroState.update { it.copy(focusMinutes = value) }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        pomodoroState.update { it.copy(breakMinutes = value) }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        pomodoroState.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        pomodoroState.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        pomodoroState.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAppTheme(theme: AppTheme) {
        initState.update { it.copy(appTheme = theme) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        pomodoroState.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        initState.update { it.copy(hasActiveSession = value) }
    }

    fun emit(preferences: PomodoroPreferences) {
        pomodoroState.value = preferences
    }
}
