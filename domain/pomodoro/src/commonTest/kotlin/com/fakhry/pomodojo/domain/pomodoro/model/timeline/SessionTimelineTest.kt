package com.fakhry.pomodojo.domain.pomodoro.model.timeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

private const val MINUTE = 60_000L
private const val START = 1_000_000L

class SessionTimelineTest {
    @Test
    fun `started runs the first segment from now`() {
        val timeline = plan().started(START)

        assertEquals(
            TimelinePhase.Running(index = 0, remainingMs = 25 * MINUTE),
            timeline.phaseAt(START),
        )
        assertEquals(START + 25 * MINUTE, timeline.segments[0].timer.finishedInMillis)
    }

    @Test
    fun `plan has not started and started is a no-op once running`() {
        val running = plan().started(START)

        assertEquals(TimelinePhase.NotStarted, plan().phaseAt(START))
        assertSame(running, running.started(START + MINUTE))
    }

    @Test
    fun `elapsedAt parks at the gate when a mid-session segment ends`() {
        val parked = plan().started(START).elapsedAt(START + 25 * MINUTE)

        assertEquals(TimelinePhase.AtGate(finishedIndex = 0), parked.phaseAt(START + 30 * MINUTE))
        assertEquals(TimerStatusDomain.COMPLETED, parked.segments[0].timerStatus)
        assertEquals(1f, parked.segments[0].timer.progress)
        assertEquals(TimerStatusDomain.INITIAL, parked.segments[1].timerStatus)
    }

    @Test
    fun `elapsedAt before the end changes nothing`() {
        val running = plan().started(START)

        assertSame(running, running.elapsedAt(START + 25 * MINUTE - 1))
    }

    @Test
    fun `last segment ending completes the timeline`() {
        val lastRunning = plan().started(START).elapsedAt(START + 25 * MINUTE)
            .continuedAt(START + 40 * MINUTE)

        assertEquals(TimelinePhase.Complete, lastRunning.phaseAt(START + 45 * MINUTE))
    }

    @Test
    fun `resume shifts the end by the paused duration`() {
        val resumed = plan().started(START)
            .pausedAt(START + 10 * MINUTE)
            .resumedAt(START + 13 * MINUTE)

        assertEquals(START + 28 * MINUTE, resumed.segments[0].timer.finishedInMillis)
        assertEquals(
            TimelinePhase.Running(index = 0, remainingMs = 15 * MINUTE),
            resumed.phaseAt(START + 13 * MINUTE),
        )
    }

    @Test
    fun `paused time is frozen`() {
        val paused = plan().started(START).pausedAt(START + 10 * MINUTE)

        assertEquals(
            TimelinePhase.Paused(index = 0, remainingMs = 15 * MINUTE),
            paused.phaseAt(START + 60 * MINUTE),
        )
    }

    @Test
    fun `pause in the final second opens the gate`() {
        val paused = plan().started(START).pausedAt(START + 25 * MINUTE)

        assertEquals(TimelinePhase.AtGate(finishedIndex = 0), paused.phaseAt(START + 25 * MINUTE))
    }

    @Test
    fun `skipped segment keeps partial credit and the next one starts`() {
        val skipped = plan().started(START).skippedAt(START + 10 * MINUTE)

        assertEquals(TimerStatusDomain.COMPLETED, skipped.segments[0].timerStatus)
        assertEquals(0.4f, skipped.segments[0].timer.progress)
        assertEquals(
            TimelinePhase.Running(index = 1, remainingMs = 5 * MINUTE),
            skipped.phaseAt(START + 10 * MINUTE),
        )
    }

    @Test
    fun `skipping while paused does not count paused time`() {
        val skipped = plan().started(START)
            .pausedAt(START + 10 * MINUTE)
            .skippedAt(START + 20 * MINUTE)

        assertEquals(0.4f, skipped.segments[0].timer.progress)
    }

    @Test
    fun `skipping the last segment completes the timeline`() {
        val skipped = plan().started(START).skippedAt(START + 25 * MINUTE - 1)
            .skippedAt(START + 26 * MINUTE)

        assertEquals(TimelinePhase.Complete, skipped.phaseAt(START + 26 * MINUTE))
    }

    @Test
    fun `continue starts the next segment from now`() {
        val continued = plan().started(START).elapsedAt(START + 25 * MINUTE)
            .continuedAt(START + 40 * MINUTE)

        assertEquals(START + 45 * MINUTE, continued.segments[1].timer.finishedInMillis)
    }

    @Test
    fun `illegal transitions leave the timeline unchanged`() {
        val running = plan().started(START)

        assertSame(running, running.resumedAt(START + MINUTE))
        assertSame(running, running.continuedAt(START + MINUTE))
    }

    @Test
    fun `finish credits the running segment for the time it ran`() {
        val finished = plan().started(START).finishedAt(START + 5 * MINUTE)

        assertEquals(TimerStatusDomain.COMPLETED, finished.segments[0].timerStatus)
        assertEquals(0.2f, finished.segments[0].timer.progress)
        assertEquals(TimerStatusDomain.INITIAL, finished.segments[1].timerStatus)
    }

    @Test
    fun `completion summary weighs minutes by progress and counts only full focus`() {
        val timeline = plan(cycles = 2).started(START)
            .elapsedAt(START + 25 * MINUTE)
            .continuedAt(START + 25 * MINUTE)
            .skippedAt(START + 27 * MINUTE)
            .finishedAt(START + 37 * MINUTE)

        assertEquals(
            CompletionSummary(focusMinutes = 35, breakMinutes = 2, cyclesFinished = 1),
            timeline.completionSummary(),
        )
    }
}

private fun plan(cycles: Int = 1) = TimelineDomain(
    segments = (1..cycles).flatMap { cycle ->
        listOf(
            segment(TimerType.FOCUS, cycle, 25 * MINUTE),
            segment(TimerType.SHORT_BREAK, cycle, 5 * MINUTE),
        )
    },
)

private fun segment(type: TimerType, cycle: Int, duration: Long) = TimerSegmentsDomain(
    type = type,
    cycleNumber = cycle,
    timer = TimerDomain(durationEpochMs = duration),
)
