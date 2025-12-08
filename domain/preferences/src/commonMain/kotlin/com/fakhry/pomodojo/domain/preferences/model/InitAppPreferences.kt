package com.fakhry.pomodojo.domain.preferences.model

data class InitAppPreferences(
    val appTheme: String = "dark",
    val hasActiveSession: Boolean = false,
)
