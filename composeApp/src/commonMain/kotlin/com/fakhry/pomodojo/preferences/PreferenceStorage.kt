package com.fakhry.pomodojo.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val data: Flow<PomodoroPreferences>
    suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences)
}

fun platformPreferenceStorage(): PreferenceStorage = createPreferenceStorage()
