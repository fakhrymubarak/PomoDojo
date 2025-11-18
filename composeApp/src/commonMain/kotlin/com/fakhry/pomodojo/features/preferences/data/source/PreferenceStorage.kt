package com.fakhry.pomodojo.features.preferences.data.source

import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val preferences: Flow<PomodoroPreferences>
    val initPreferences: Flow<InitAppPreferences>

    suspend fun updatePreferences(transform: (PomodoroPreferences) -> PomodoroPreferences)

    suspend fun updateInitPreferences(transform: (InitAppPreferences) -> InitAppPreferences)
}
