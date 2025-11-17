package com.fakhry.pomodojo.features.focus.domain.model

/**
 * Summary data for displaying session completion notification.
 * Contains aggregated statistics about the completed pomodoro session.
 */
data class CompletionNotificationSummary(
    val sessionId: String,
    val totalFocusMinutes: Int,
    val totalBreakMinutes: Int,
    val completedCycles: Int,
)
