package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.features.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakePreferenceStorage : PreferenceStorage {
    private val pomodoroState = MutableStateFlow(PomodoroPreferences())
    private val initState = MutableStateFlow(InitAppPreferences())

    override val preferences: Flow<PomodoroPreferences> = pomodoroState
    override val initPreferences: Flow<InitAppPreferences> = initState

    override suspend fun updatePreferences(
        transform: (PomodoroPreferences) -> PomodoroPreferences,
    ) {
        pomodoroState.update(transform)
    }

    override suspend fun updateInitPreferences(
        transform: (InitAppPreferences) -> InitAppPreferences,
    ) {
        initState.update(transform)
    }
}
