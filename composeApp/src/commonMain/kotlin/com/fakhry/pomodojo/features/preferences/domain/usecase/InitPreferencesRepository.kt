package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import kotlinx.coroutines.flow.Flow

interface InitPreferencesRepository {
    val initPreferences: Flow<InitAppPreferences>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateHasActiveSession(value: Boolean)
}
