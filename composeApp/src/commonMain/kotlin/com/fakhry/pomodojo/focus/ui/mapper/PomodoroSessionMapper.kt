package com.fakhry.pomodojo.focus.ui.mapper

import com.fakhry.pomodojo.focus.domain.model.CompletionNotificationSummary
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.ui.PomodoroSessionUiState
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import kotlinx.collections.immutable.toPersistentList

fun PomodoroSessionDomain.toUiState(
    segments: List<TimelineSegmentUi>,
    activeIndex: Int,
    isComplete: Boolean,
): PomodoroSessionUiState {
    val active = segments.getOrNull(activeIndex) ?: TimelineSegmentUi()
    return PomodoroSessionUiState(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPauseEpochMs = elapsedPauseEpochMs,
        activeSegment = active,
        timeline = TimelineUiModel(
            segments = segments.toPersistentList(),
            hourSplits = timeline.hourSplits.toPersistentList(),
        ),
        quote = quote,
        isShowConfirmEndDialog = false,
        isComplete = isComplete,
    )
}

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
