package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import com.fakhry.pomodojo.preferences.domain.TimelinePreviewBuilder
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesDomainTest {

    private val timelineBuilder = TimelinePreviewBuilder()
    private val cascadeResolver = PreferenceCascadeResolver()

    @Test
    fun `timeline builder includes long break when enabled`() {
        val preferences = PomodoroPreferences(
            repeatCount = 4,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = true,
            longBreakAfter = 4,
            longBreakMinutes = 10,
        )

        val segments = timelineBuilder.build(preferences)

        assertEquals(8, segments.size)
        assertEquals(TimelineSegment.Focus(durationMinutes = 25), segments.first())
        assertEquals(TimelineSegment.ShortBreak(durationMinutes = 5), segments[1])
        assertEquals(TimelineSegment.LongBreak(durationMinutes = 10), segments.last())
    }

    @Test
    fun `timeline builder omits long break when disabled`() {
        val preferences = PomodoroPreferences(
            repeatCount = 4,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = false,
            longBreakAfter = 4,
            longBreakMinutes = 10,
        )

        val segments = timelineBuilder.build(preferences)

        assertEquals(7, segments.size)
        assertEquals(TimelineSegment.Focus(durationMinutes = 25), segments.first())
        assertEquals(TimelineSegment.ShortBreak(durationMinutes = 5), segments[1])
        assertEquals(TimelineSegment.Focus(durationMinutes = 25), segments.last())
        assertEquals(3, segments.count { it is TimelineSegment.ShortBreak })
        assertFalse(segments.any { it is TimelineSegment.LongBreak })
    }

    @Test
    fun `timeline builder inserts long breaks at configured interval`() {
        val preferences = PomodoroPreferences(
            repeatCount = 6,
            focusMinutes = 50,
            breakMinutes = 10,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 20,
        )

        val segments = timelineBuilder.build(preferences)
        val longBreakCount = segments.count { it is TimelineSegment.LongBreak }

        assertEquals(3, longBreakCount) // after 2nd, 4th, and 6th focus
        assertEquals(TimelineSegment.LongBreak(20), segments[3])
        assertEquals(TimelineSegment.LongBreak(20), segments[7])
        assertEquals(TimelineSegment.LongBreak(20), segments.last())
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
