package com.fakhry.pomodojo.preferences.data.source

import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val preferences: Flow<PomodoroPreferences>
    suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences)
}