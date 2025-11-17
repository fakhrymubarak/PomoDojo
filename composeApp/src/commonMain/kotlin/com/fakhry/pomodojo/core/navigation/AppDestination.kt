package com.fakhry.pomodojo.core.navigation

import kotlinx.serialization.Serializable

object AppDestination {
    @Serializable
    data object Dashboard

    @Serializable
    data object Preferences

    @Serializable
    data object PomodoroSession

    @Serializable
    data class PomodoroComplete(
        val totalCyclesFinished: Int = 0,
        val totalFocusMinutes: Int = 0,
        val totalBreakMinutes: Int = 0,
    )
}
