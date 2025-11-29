package com.fakhry.pomodojo.shared.domain.model.focus

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
