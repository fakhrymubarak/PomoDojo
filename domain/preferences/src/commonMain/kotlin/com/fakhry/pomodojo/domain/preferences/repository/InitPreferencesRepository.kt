package com.fakhry.pomodojo.domain.preferences.repository

import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.InitAppPreferences
import kotlinx.coroutines.flow.Flow

interface InitPreferencesRepository {
    val initPreferences: Flow<InitAppPreferences>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateHasActiveSession(value: Boolean)
}
