package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences
import com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakePreferenceStorage : PreferenceStorage {
    internal val pomodoroState = MutableStateFlow(PomodoroPreferences())
    private val initState = MutableStateFlow(InitAppPreferences())
    private val activeState = MutableStateFlow(PomodoroSessionDomain())

    override val preferences: Flow<PomodoroPreferences> = pomodoroState
    override val initPreferences: Flow<InitAppPreferences> = initState
    override val activeSession: Flow<PomodoroSessionDomain> = activeState

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

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        activeState.update { snapshot }
    }

    override suspend fun clearActiveSession() {
        activeState.update { PomodoroSessionDomain() }
    }
}
