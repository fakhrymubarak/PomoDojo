package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesDomainTest {

    private val timelineBuilder = BuildFocusTimelineUseCase()
    private val cascadeResolver = PreferenceCascadeResolver()

    @Test
    fun `timeline builder inserts long break before subsequent focus`() {
        val preferences = PreferencesDomain(
            repeatCount = 5,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 15,
        )

        val segments = timelineBuilder(preferences)
        val longBreaks = segments.filterIsInstance<TimelineSegmentDomain.LongBreak>()

        assertEquals(9, segments.size)
        assertEquals(2, longBreaks.size)
        assertEquals(15, longBreaks.first().duration)
        assertEquals(15, longBreaks.last().duration)

        // Ensure long breaks are placed after every second focus except the final one.
        assertTrue(segments[3] is TimelineSegmentDomain.LongBreak)
        assertTrue(segments[7] is TimelineSegmentDomain.LongBreak)
        assertEquals(5 * 25 + 2 * 15 + 2 * 5, segments.sumOf { it.duration })
    }

    @Test
    fun `timeline builder omits long break when disabled`() {
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

        val firstFocus = segments.first() as TimelineSegmentDomain.Focus
        assertEquals(25, firstFocus.duration)

        val firstBreak = segments[1] as TimelineSegmentDomain.ShortBreak
        assertEquals(5, firstBreak.duration)

        val lastFocus = segments.last() as TimelineSegmentDomain.Focus
        assertEquals(25, lastFocus.duration)

        assertEquals(3, segments.count { it is TimelineSegmentDomain.ShortBreak })
        assertFalse(segments.any { it is TimelineSegmentDomain.LongBreak })
    }

    @Test
    fun `timeline builder inserts long breaks at configured interval`() {
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
            .filter { it.value is TimelineSegmentDomain.LongBreak }
            .map { it.index }

        assertEquals(listOf(3, 7), longBreaks) // after 2nd and 4th focus
        assertEquals(20, (segments[3] as TimelineSegmentDomain.LongBreak).duration)
        assertEquals(20, (segments[7] as TimelineSegmentDomain.LongBreak).duration)
        assertEquals(preferences.repeatCount * 2 - 1, segments.size) // focus blocks + breaks between them
    }

    @Test
    fun `focus cascade sets dependent timers according to spec`() {
        val cascade = cascadeResolver.resolveForFocus(minutes = 50)

        assertEquals(10, cascade.breakMinutes)
        assertEquals(2, cascade.longBreakAfterCount)
        assertEquals(20, cascade.longBreakMinutes)
    }

    @Test
    fun `break cascade sets long break duration`() {
        val cascade = cascadeResolver.resolveForBreak(minutes = 2)

        assertEquals(4, cascade.longBreakMinutes)
    }

    @Test
    fun `preferences validator enforces allowed values`() {
        assertTrue(PreferencesValidator.isValidRepeatCount(4))
        assertFalse(PreferencesValidator.isValidRepeatCount(1))
        assertTrue(PreferencesValidator.isValidFocusMinutes(25))
        assertFalse(PreferencesValidator.isValidFocusMinutes(30))
        assertTrue(PreferencesValidator.isValidBreakMinutes(5))
        assertFalse(PreferencesValidator.isValidBreakMinutes(7))
        assertTrue(PreferencesValidator.isValidLongBreakAfter(6))
        assertFalse(PreferencesValidator.isValidLongBreakAfter(3))
        assertTrue(PreferencesValidator.isValidLongBreakMinutes(20))
        assertFalse(PreferencesValidator.isValidLongBreakMinutes(15))
    }
}
