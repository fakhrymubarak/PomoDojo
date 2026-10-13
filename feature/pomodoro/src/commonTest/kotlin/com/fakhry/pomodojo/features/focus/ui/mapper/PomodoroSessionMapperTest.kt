package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.CompletionSummary
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val MINUTE_MS = 60_000L

class PomodoroSessionMapperTest {

    @Test
    fun `running session maps to ui state with the running segment active`() {
        val domain = PomodoroSessionDomain(
            totalCycle = 3,
            startedAtEpochMs = 1234L,
            timeline = TimelineDomain(
                segments = listOf(
                    segment(
                        TimerType.FOCUS,
                        TimerStatusDomain.RUNNING,
                        finishedAt = 25 * MINUTE_MS,
                    ),
                    segment(TimerType.SHORT_BREAK, TimerStatusDomain.INITIAL),
                ),
                hourSplits = listOf(0, 25, 30),
            ),
            quote = QuoteContent(id = "quote", text = "Keep going"),
        )

        val uiState = domain.toUiState(now = 10 * MINUTE_MS)

        assertEquals(domain.totalCycle, uiState.totalCycle)
        assertEquals(domain.startedAtEpochMs, uiState.startedAtEpochMs)
        assertEquals(TimerTypeUi.FOCUS, uiState.activeSegment.type)
        assertEquals("15:00", uiState.activeSegment.timer.formattedTime)
        assertEquals(0.4f, uiState.activeSegment.timer.progress)
        assertEquals(domain.timeline.hourSplits, uiState.timeline.hourSplits.toList())
        assertEquals(domain.quote, uiState.quote)
        assertFalse(uiState.awaitingContinue)
        assertFalse(uiState.isComplete)
    }

    @Test
    fun `session at the gate shows the finished segment and the next one`() {
        val domain = PomodoroSessionDomain(
            timeline = TimelineDomain(
                segments = listOf(
                    segment(TimerType.FOCUS, TimerStatusDomain.COMPLETED, progress = 1f),
                    segment(TimerType.SHORT_BREAK, TimerStatusDomain.INITIAL),
                ),
            ),
        )

        val uiState = domain.toUiState(now = 0L)

        assertTrue(uiState.awaitingContinue)
        assertEquals(TimerStatusUi.COMPLETED, uiState.activeSegment.timerStatus)
        assertEquals(TimerTypeUi.FOCUS, uiState.finishedPhaseType)
        assertEquals(TimerTypeUi.SHORT_BREAK, uiState.nextPhaseType)
        assertEquals(25 * MINUTE_MS, uiState.nextPhaseDurationMs)
    }

    @Test
    fun `completed segment keeps the credit it was given`() {
        val domain = PomodoroSessionDomain(
            timeline = TimelineDomain(
                segments = listOf(
                    segment(TimerType.FOCUS, TimerStatusDomain.COMPLETED, progress = 0.4f),
                    segment(
                        TimerType.SHORT_BREAK,
                        TimerStatusDomain.RUNNING,
                        finishedAt = MINUTE_MS,
                    ),
                ),
            ),
        )

        val uiState = domain.toUiState(now = 0L)

        assertEquals(0.4f, uiState.timeline.segments[0].timer.progress)
    }

    @Test
    fun `empty session uses the default active segment`() {
        val uiState = PomodoroSessionDomain().toUiState(now = 0L)

        assertEquals(TimelineSegmentUi(), uiState.activeSegment)
    }

    @Test
    fun `completion summary maps to the completion ui state`() {
        val summary = CompletionSummary(focusMinutes = 37, breakMinutes = 10, cyclesFinished = 1)

        assertEquals(
            PomodoroCompletionUiState(
                totalCyclesFinished = 1,
                totalFocusMinutes = 37,
                totalBreakMinutes = 10,
            ),
            summary.toUiState(),
        )
    }
}

private fun segment(
    type: TimerType,
    status: TimerStatusDomain,
    finishedAt: Long = 0L,
    progress: Float = 0f,
) = TimerSegmentsDomain(
    type = type,
    timer = TimerDomain(
        progress = progress,
        durationEpochMs = 25 * MINUTE_MS,
        finishedInMillis = finishedAt,
    ),
    timerStatus = status,
)
