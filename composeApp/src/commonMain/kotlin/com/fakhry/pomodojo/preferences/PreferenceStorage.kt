package com.fakhry.pomodojo.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val preferences: Flow<PomodoroPreferences>
    suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences)
}