package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.shared.domain.model.preferences.AppTheme
import com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences
import kotlinx.coroutines.flow.Flow

interface InitPreferencesRepository {
    val initPreferences: Flow<InitAppPreferences>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateHasActiveSession(value: Boolean)
}
