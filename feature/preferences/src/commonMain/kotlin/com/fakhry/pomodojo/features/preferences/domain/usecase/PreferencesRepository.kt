package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<PomodoroPreferences>

    suspend fun updateRepeatCount(value: Int)

    suspend fun updateFocusMinutes(value: Int)

    suspend fun updateBreakMinutes(value: Int)

    suspend fun updateLongBreakEnabled(enabled: Boolean)

    suspend fun updateLongBreakAfter(value: Int)

    suspend fun updateLongBreakMinutes(value: Int)

    suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean)
}
