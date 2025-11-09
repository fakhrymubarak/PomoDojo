package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BuildFocusTimelineUseCaseTest {

    private val timelineBuilder = BuildTimerSegmentsUseCase()

    @Test
    fun `inserts long break before subsequent focus`() {
        val preferences = PreferencesDomain(
            repeatCount = 5,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 15,
        )

        val segments = timelineBuilder(0L, preferences)
        val longBreaks = segments.filter { it.type == TimerType.LONG_BREAK }

        assertEquals(9, segments.size)
        assertEquals(2, longBreaks.size)
        assertEquals(15, longBreaks.first().durationMinutes())
        assertEquals(15, longBreaks.last().durationMinutes())

        // Ensure long breaks are placed after every second focus except the final one.
        assertEquals(TimerType.LONG_BREAK, segments[3].type)
        assertEquals(TimerType.LONG_BREAK, segments[7].type)
        assertEquals(5 * 25 + 2 * 15 + 2 * 5, segments.sumOf { it.durationMinutes() })
    }

    @Test
    fun `omits long break when disabled`() {
        val preferences = PreferencesDomain(
            repeatCount = 4,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = false,
            longBreakAfter = 4,
            longBreakMinutes = 10,
        )

        val segments = timelineBuilder(0L, preferences)
        assertEquals(7, segments.size)

        val firstSegment = segments.first()
        assertEquals(TimerType.FOCUS, firstSegment.type)
        assertEquals(25, firstSegment.durationMinutes())

        val firstBreak = segments[1]
        assertEquals(TimerType.SHORT_BREAK, firstBreak.type)
        assertEquals(5, firstBreak.durationMinutes())

        val lastFocus = segments.last()
        assertEquals(TimerType.FOCUS, lastFocus.type)
        assertEquals(25, lastFocus.durationMinutes())

        assertEquals(3, segments.count { it.type == TimerType.SHORT_BREAK })
        assertFalse(segments.any { it.type == TimerType.LONG_BREAK })
    }

    @Test
    fun `inserts long breaks at configured interval`() {
        val preferences = PreferencesDomain(
            repeatCount = 6,
            focusMinutes = 50,
            breakMinutes = 10,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 20,
        )

        val segments = timelineBuilder(0L, preferences)
        val longBreaks = segments.withIndex()
            .filter { it.value.type == TimerType.LONG_BREAK }
            .map { it.index }

        assertEquals(listOf(3, 7), longBreaks) // after 2nd and 4th focus
        assertEquals(20, segments[3].durationMinutes())
        assertEquals(20, segments[7].durationMinutes())
        assertEquals(
            preferences.repeatCount * 2 - 1,
            segments.size
        ) // focus blocks + breaks between them
    }
}

private fun TimerSegmentsDomain.durationMinutes(): Int =
    (timer.durationEpochMs / 60_000L).toInt()
