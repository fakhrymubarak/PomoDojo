package com.fakhry.pomodojo.core.notification.mapper

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.notification.CompletionNotificationSummary
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType

/**
 * Converts PomodoroSessionDomain to CompletionNotificationSummary.
 * Calculates total focus and break time from all segments.
 */
fun PomodoroSessionDomain.toCompletionSummary(): CompletionNotificationSummary {
    val focusSegments = timeline.segments.filter { it.type == TimerType.FOCUS }
    val breakSegments = timeline.segments.filter {
        it.type == TimerType.SHORT_BREAK || it.type == TimerType.LONG_BREAK
    }

    val totalFocusMs = focusSegments.sumOf { it.timer.durationEpochMs }
    val totalBreakMs = breakSegments.sumOf { it.timer.durationEpochMs }

    return CompletionNotificationSummary(
        sessionId = sessionId(),
        totalFocusMinutes = (totalFocusMs / 60_000L).toInt(),
        totalBreakMinutes = (totalBreakMs / 60_000L).toInt(),
        completedCycles = totalCycle,
    )
}
