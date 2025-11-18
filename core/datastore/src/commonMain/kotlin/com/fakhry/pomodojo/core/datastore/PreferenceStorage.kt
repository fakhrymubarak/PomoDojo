package com.fakhry.pomodojo.core.datastore

import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences
import com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val preferences: Flow<PomodoroPreferences>
    val initPreferences: Flow<InitAppPreferences>
    val activeSession: Flow<PomodoroSessionDomain>

    suspend fun updatePreferences(transform: (PomodoroPreferences) -> PomodoroPreferences)

    suspend fun updateInitPreferences(transform: (InitAppPreferences) -> InitAppPreferences)
    suspend fun saveActiveSession(snapshot: PomodoroSessionDomain)
    suspend fun clearActiveSession()
}
