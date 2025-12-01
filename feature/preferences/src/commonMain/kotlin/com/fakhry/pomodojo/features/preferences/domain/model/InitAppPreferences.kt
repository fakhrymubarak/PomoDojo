package com.fakhry.pomodojo.features.preferences.domain.model

data class InitAppPreferences(
    val appTheme: String = "dark",
    val hasActiveSession: Boolean = false,
)
