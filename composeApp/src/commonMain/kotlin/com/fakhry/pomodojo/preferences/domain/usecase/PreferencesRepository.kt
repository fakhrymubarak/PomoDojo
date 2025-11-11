package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<PreferencesDomain>

    suspend fun updateRepeatCount(value: Int)

    suspend fun updateFocusMinutes(value: Int)

    suspend fun updateBreakMinutes(value: Int)

    suspend fun updateLongBreakEnabled(enabled: Boolean)

    suspend fun updateLongBreakAfter(value: Int)

    suspend fun updateLongBreakMinutes(value: Int)

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean)
}
