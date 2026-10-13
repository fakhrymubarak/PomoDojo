package com.fakhry.pomodojo.domain.pomodoro.model.timeline

import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimelinePlanTest {
    @Test
    fun `every cycle is a focus and a break with a long break at the interval`() {
        val plan = planTimeline(
            PomodoroPreferences(
                repeatCount = 5,
                focusMinutes = 25,
                breakMinutes = 5,
                longBreakEnabled = true,
                longBreakAfter = 2,
                longBreakMinutes = 15,
            ),
        )

        assertEquals(
            listOf(
                TimerType.FOCUS, TimerType.SHORT_BREAK,
                TimerType.FOCUS, TimerType.LONG_BREAK,
                TimerType.FOCUS, TimerType.SHORT_BREAK,
                TimerType.FOCUS, TimerType.LONG_BREAK,
                TimerType.FOCUS, TimerType.SHORT_BREAK,
            ),
            plan.segments.map { it.type },
        )
        assertEquals(listOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5), plan.segments.map { it.cycleNumber })
        assertEquals(15, plan.segments[3].durationMinutes())
        assertEquals(5, plan.segments[1].durationMinutes())
    }

    @Test
    fun `plan has not started`() {
        val plan = planTimeline(PomodoroPreferences())

        assertTrue(plan.segments.all { it.timerStatus == TimerStatusDomain.INITIAL })
        assertEquals(TimelinePhase.NotStarted, plan.phaseAt(now = 0L))
    }

    @Test
    fun `long breaks disabled leaves only short breaks`() {
        val plan = planTimeline(
            PomodoroPreferences(repeatCount = 4, longBreakEnabled = false, longBreakAfter = 2),
        )

        assertEquals(4, plan.segments.count { it.type == TimerType.SHORT_BREAK })
        assertEquals(0, plan.segments.count { it.type == TimerType.LONG_BREAK })
    }

    @Test
    fun `hour splits are the total in full hours plus the remainder`() {
        // 6*25 + 3*15 + 3*5 = 210 min
        val withLongBreaks = PomodoroPreferences(
            repeatCount = 6,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 15,
        )
        // 1*45 + 1*10 = 55 min
        val underAnHour = PomodoroPreferences(
            repeatCount = 1,
            focusMinutes = 45,
            longBreakAfter = 1,
            longBreakMinutes = 10,
        )
        // 2*25 + 2*5 = 60 min
        val exactHour = PomodoroPreferences(repeatCount = 2, longBreakEnabled = false)

        assertEquals(listOf(60, 60, 60, 30), planTimeline(withLongBreaks).hourSplits)
        assertEquals(listOf(55), planTimeline(underAnHour).hourSplits)
        assertEquals(listOf(60), planTimeline(exactHour).hourSplits)
    }

    @Test
    fun `values the ui cannot produce are tolerated`() {
        val noLongBreaks = planTimeline(PomodoroPreferences(repeatCount = 2, longBreakAfter = 0))
        val empty = planTimeline(PomodoroPreferences(repeatCount = 0))

        assertEquals(0, noLongBreaks.segments.count { it.type == TimerType.LONG_BREAK })
        assertEquals(TimelineDomain(), empty)
    }
}

private fun TimerSegmentsDomain.durationMinutes(): Int = (timer.durationEpochMs / 60_000L).toInt()
