package com.fakhry.pomodojo.focus.ui.mapper

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.preferences.ui.model.TimerUi
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

private const val MINUTE_MS = 60_000L

class PomodoroSessionMapperTest {

    @Test
    fun `domain session maps to ui state with timeline metadata`() {
        val segments = listOf(
            TimelineSegmentUi(type = TimerType.FOCUS, cycleNumber = 1),
            TimelineSegmentUi(type = TimerType.SHORT_BREAK, cycleNumber = 1),
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
    fun `domain completion summary aggregates focus and break minutes`() {
        val domain = PomodoroSessionDomain(
            totalCycle = 2,
            startedAtEpochMs = 42L,
            timeline = TimelineDomain(
                segments = listOf(
                    domainSegment(TimerType.FOCUS, 25),
                    domainSegment(TimerType.SHORT_BREAK, 5),
                    domainSegment(TimerType.FOCUS, 15),
                    domainSegment(TimerType.LONG_BREAK, 10),
                ),
            ),
        )

        val summary = domain.toCompletionSummary()

        assertEquals("42", summary.sessionId)
        assertEquals(40, summary.totalFocusMinutes)
        assertEquals(15, summary.totalBreakMinutes)
        assertEquals(domain.totalCycle, summary.completedCycles)
    }

    @Test
    fun `ui state completion summary uses elapsed progress`() {
        val timeline = TimelineUiModel(
            segments = persistentListOf(
                uiSegment(TimerType.FOCUS, TimerStatusDomain.COMPLETED, 25, 1f),
                uiSegment(TimerType.SHORT_BREAK, TimerStatusDomain.COMPLETED, 10, 1f),
                uiSegment(TimerType.FOCUS, TimerStatusDomain.RUNNING, 25, 0.5f),
                uiSegment(TimerType.SHORT_BREAK, TimerStatusDomain.INITIAL, 15, 0f),
                uiSegment(TimerType.LONG_BREAK, TimerStatusDomain.INITIAL, 5, 0f),
                uiSegment(TimerType.FOCUS, TimerStatusDomain.INITIAL, 25, 0f),
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

    private fun domainSegment(type: TimerType, minutes: Int) = TimerSegmentsDomain(
        type = type,
        timer = TimerDomain(durationEpochMs = minutes.toLong() * MINUTE_MS),
    )

    private fun uiSegment(
        type: TimerType,
        status: TimerStatusDomain,
        minutes: Int,
        progress: Float,
    ) = TimelineSegmentUi(
        type = type,
        timerStatus = status,
        timer = TimerUi(
            durationEpochMs = minutes.toLong() * MINUTE_MS,
            progress = progress,
        ),
    )
}
