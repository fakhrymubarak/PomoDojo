package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BuildTimerSegmentsUseCaseTest {
    private val timelineBuilder = BuildTimerSegmentsUseCase()

    @Test
    fun `inserts long break before subsequent focus`() {
        val preferences =
            PomodoroPreferences(
                repeatCount = 5,
                focusMinutes = 25,
                breakMinutes = 5,
                longBreakEnabled = true,
                longBreakAfter = 2,
                longBreakMinutes = 15,
            )

        val segments = timelineBuilder(0L, preferences)
        val longBreaks = segments.filter { it.type == TimerType.LONG_BREAK }

        // Now includes break after last focus: 5 focus + 2 long breaks + 3 short breaks = 10
        assertEquals(10, segments.size)
        assertEquals(2, longBreaks.size) // After 2nd and 4th focus only
        assertEquals(15, longBreaks.first().durationMinutes())
        assertEquals(15, longBreaks.last().durationMinutes())

        // Ensure long breaks are placed after every second focus
        assertEquals(TimerType.LONG_BREAK, segments[3].type)
        assertEquals(TimerType.LONG_BREAK, segments[7].type)
        assertEquals(5 * 25 + 2 * 15 + 3 * 5, segments.sumOf { it.durationMinutes() })
    }

    @Test
    fun `omits long break when disabled`() {
        val preferences =
            PomodoroPreferences(
                repeatCount = 4,
                focusMinutes = 25,
                breakMinutes = 5,
                longBreakEnabled = false,
                longBreakAfter = 4,
                longBreakMinutes = 10,
            )

        val segments = timelineBuilder(0L, preferences)
        // Now includes break after last focus: 4 focus + 4 short breaks = 8
        assertEquals(8, segments.size)

        val firstSegment = segments.first()
        assertEquals(TimerType.FOCUS, firstSegment.type)
        assertEquals(25, firstSegment.durationMinutes())

        val firstBreak = segments[1]
        assertEquals(TimerType.SHORT_BREAK, firstBreak.type)
        assertEquals(5, firstBreak.durationMinutes())

        val lastBreak = segments.last()
        assertEquals(TimerType.SHORT_BREAK, lastBreak.type) // Last segment is now a break
        assertEquals(5, lastBreak.durationMinutes())

        assertEquals(4, segments.count { it.type == TimerType.SHORT_BREAK })
        assertFalse(segments.any { it.type == TimerType.LONG_BREAK })
    }

    @Test
    fun `inserts long breaks at configured interval`() {
        val preferences =
            PomodoroPreferences(
                repeatCount = 6,
                focusMinutes = 50,
                breakMinutes = 10,
                longBreakEnabled = true,
                longBreakAfter = 2,
                longBreakMinutes = 20,
            )

        val segments = timelineBuilder(0L, preferences)
        val longBreaks =
            segments
                .withIndex()
                .filter { it.value.type == TimerType.LONG_BREAK }
                .map { it.index }

        // Long breaks after 2nd, 4th, and 6th (last) focus
        assertEquals(listOf(3, 7, 11), longBreaks)
        assertEquals(20, segments[3].durationMinutes())
        assertEquals(20, segments[7].durationMinutes())
        assertEquals(20, segments[11].durationMinutes())
        assertEquals(
            preferences.repeatCount * 2,
            segments.size,
        ) // focus blocks + breaks after each
    }
}

private fun TimerSegmentsDomain.durationMinutes(): Int = (timer.durationEpochMs / 60_000L).toInt()
