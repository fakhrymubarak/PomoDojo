package com.fakhry.pomodojo.focus.ui

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.ui.mapper.toTimelineUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel

@Immutable
data class PomodoroSessionUiState(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val activeSegment: TimelineSegmentUi = TimelineSegmentUi(),
    val timeline: TimelineUiModel = TimelineUiModel(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
    val isShowConfirmEndDialog: Boolean = false,
    val isComplete: Boolean = false,
)

@Immutable
data class PomodoroHeaderUiState(
    val totalCycle: Int = 0,
    val activeSegment: TimelineSegmentUi = TimelineSegmentUi(),
)

sealed class PomodoroSessionSideEffect {
    data class ShowEndSessionDialog(val isShown: Boolean) : PomodoroSessionSideEffect()
    object OnSessionComplete : PomodoroSessionSideEffect()
}

fun PomodoroSessionDomain.toPomodoroUiSessionUi(now: Long): PomodoroSessionUiState {
    val timeline = timeline.toTimelineUi(now, progress = 0f)
    return PomodoroSessionUiState(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPauseEpochMs = elapsedPauseEpochMs,
        activeSegment = timeline.segments.first(),
        timeline = timeline,
        quote = quote,
        isShowConfirmEndDialog = false,
        isComplete = false,
    )
}
