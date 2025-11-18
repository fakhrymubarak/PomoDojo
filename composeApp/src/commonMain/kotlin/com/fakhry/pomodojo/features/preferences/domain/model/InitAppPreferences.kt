package com.fakhry.pomodojo.features.preferences.domain.model

data class InitAppPreferences(
    val appTheme: AppTheme = AppTheme.DARK,
    val hasActiveSession: Boolean = false,
)
