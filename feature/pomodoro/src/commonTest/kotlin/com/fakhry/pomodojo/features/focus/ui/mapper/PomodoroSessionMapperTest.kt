package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.model.TimerUi
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

private const val MINUTE_MS = 60_000L

class PomodoroSessionMapperTest {

    @Test
    fun `domain session maps to ui state with timeline metadata`() {
        val segments = listOf(
            TimelineSegmentUi(type = TimerTypeUi.FOCUS, cycleNumber = 1),
            TimelineSegmentUi(type = TimerTypeUi.SHORT_BREAK, cycleNumber = 1),
        )
        val domain = PomodoroSessionDomain(
            totalCycle = 3,
            startedAtEpochMs = 1234L,
            elapsedPauseEpochMs = 200L,
            timeline = TimelineDomain(hourSplits = listOf(0, 25, 30)),
            quote = QuoteContent(id = "quote", text = "Keep going"),
        )

        val uiState = domain.toUiState(
            segments = segments,
            activeIndex = 1,
            isComplete = true,
        )

        assertEquals(domain.totalCycle, uiState.totalCycle)
        assertEquals(domain.startedAtEpochMs, uiState.startedAtEpochMs)
        assertEquals(domain.elapsedPauseEpochMs, uiState.elapsedPauseEpochMs)
        assertEquals(segments[1], uiState.activeSegment)
        assertEquals(segments, uiState.timeline.segments.toList())
        assertEquals(domain.timeline.hourSplits, uiState.timeline.hourSplits.toList())
        assertEquals(domain.quote, uiState.quote)
        assertEquals(true, uiState.isComplete)
        assertEquals(false, uiState.isShowConfirmEndDialog)
    }

    @Test
    fun `domain session uses default active segment when index is invalid`() {
        val uiState = PomodoroSessionDomain().toUiState(
            segments = emptyList(),
            activeIndex = 5,
            isComplete = false,
        )

        assertEquals(TimelineSegmentUi(), uiState.activeSegment)
    }

    @Test
    fun `ui state completion summary uses elapsed progress`() {
        val timeline = TimelineUiModel(
            segments = persistentListOf(
                uiSegment(TimerTypeUi.FOCUS, TimerStatusUi.COMPLETED, 25, 1f),
                uiSegment(TimerTypeUi.SHORT_BREAK, TimerStatusUi.COMPLETED, 10, 1f),
                uiSegment(TimerTypeUi.FOCUS, TimerStatusUi.RUNNING, 25, 0.5f),
                uiSegment(TimerTypeUi.SHORT_BREAK, TimerStatusUi.INITIAL, 15, 0f),
                uiSegment(TimerTypeUi.LONG_BREAK, TimerStatusUi.INITIAL, 5, 0f),
                uiSegment(TimerTypeUi.FOCUS, TimerStatusUi.INITIAL, 25, 0f),
            ),
            hourSplits = persistentListOf(0, 25),
        )
        val uiState = PomodoroSessionUiState(
            totalCycle = 4,
            timeline = timeline,
        )

        val summary = uiState.toCompletionSummary()

        assertEquals(1, summary.totalCyclesFinished)
        assertEquals(37, summary.totalFocusMinutes)
        assertEquals(10, summary.totalBreakMinutes)
    }
}

private fun uiSegment(type: TimerTypeUi, status: TimerStatusUi, minutes: Int, progress: Float) =
    TimelineSegmentUi(
        type = type,
        timerStatus = status,
        timer = TimerUi(
            durationEpochMs = minutes.toLong() * MINUTE_MS,
            progress = progress,
        ),
    )
