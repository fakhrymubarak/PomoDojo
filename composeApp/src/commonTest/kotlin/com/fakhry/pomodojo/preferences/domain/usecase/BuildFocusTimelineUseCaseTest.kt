package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineTimerDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildFocusTimelineUseCaseTest {

    private val timelineBuilder = BuildFocusTimelineUseCase()

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

        val segments = timelineBuilder(preferences)
        val longBreaks = segments.filterIsInstance<TimelineTimerDomain.LongBreak>()

        assertEquals(9, segments.size)
        assertEquals(2, longBreaks.size)
        assertEquals(15, longBreaks.first().duration)
        assertEquals(15, longBreaks.last().duration)

        // Ensure long breaks are placed after every second focus except the final one.
        assertTrue(segments[3] is TimelineTimerDomain.LongBreak)
        assertTrue(segments[7] is TimelineTimerDomain.LongBreak)
        assertEquals(5 * 25 + 2 * 15 + 2 * 5, segments.sumOf { it.duration })
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

        val segments = timelineBuilder(preferences)
        assertEquals(7, segments.size)

        val firstFocus = segments.first() as TimelineTimerDomain.Focus
        assertEquals(25, firstFocus.duration)

        val firstBreak = segments[1] as TimelineTimerDomain.ShortBreak
        assertEquals(5, firstBreak.duration)

        val lastFocus = segments.last() as TimelineTimerDomain.Focus
        assertEquals(25, lastFocus.duration)

        assertEquals(3, segments.count { it is TimelineTimerDomain.ShortBreak })
        assertFalse(segments.any { it is TimelineTimerDomain.LongBreak })
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

        val segments = timelineBuilder(preferences)
        val longBreaks = segments.withIndex()
            .filter { it.value is TimelineTimerDomain.LongBreak }
            .map { it.index }

        assertEquals(listOf(3, 7), longBreaks) // after 2nd and 4th focus
        assertEquals(20, (segments[3] as TimelineTimerDomain.LongBreak).duration)
        assertEquals(20, (segments[7] as TimelineTimerDomain.LongBreak).duration)
        assertEquals(
            preferences.repeatCount * 2 - 1,
            segments.size
        ) // focus blocks + breaks between them
    }
}
