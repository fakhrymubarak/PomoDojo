package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesDomainTest {

    private val timelineBuilder = BuildFocusTimelineUseCase()
    private val cascadeResolver = PreferenceCascadeResolver()

    @Test
    fun `timeline builder includes long break when enabled`() {
        val preferences = PreferencesDomain(
            repeatCount = 4,
            focusMinutes = 25,
            breakMinutes = 5,
            longBreakEnabled = true,
            longBreakAfter = 4,
            longBreakMinutes = 10,
        )

        val segments = timelineBuilder(preferences)

        val totalMinutes = segments.sumOf { it.duration }

        assertEquals(8, segments.size)

        val firstFocus = segments.first() as TimelineSegmentUiModel.Focus
        assertEquals(25, firstFocus.duration)
        assertEquals(
            expected = 25f / totalMinutes,
            actual = firstFocus.weight,
            absoluteTolerance = 0.0001f,
        )

        val firstBreak = segments[1] as TimelineSegmentUiModel.ShortBreak
        assertEquals(5, firstBreak.duration)
        assertEquals(
            expected = 5f / totalMinutes,
            actual = firstBreak.weight,
            absoluteTolerance = 0.0001f,
        )

        val finalLongBreak = segments.last() as TimelineSegmentUiModel.LongBreak
        assertEquals(10, finalLongBreak.duration)
        assertEquals(
            expected = 10f / totalMinutes,
            actual = finalLongBreak.weight,
            absoluteTolerance = 0.0001f,
        )
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

        val totalMinutes = segments.sumOf { it.duration }

        assertEquals(7, segments.size)

        val firstFocus = segments.first() as TimelineSegmentUiModel.Focus
        assertEquals(25, firstFocus.duration)
        assertEquals(
            expected = 25f / totalMinutes,
            actual = firstFocus.weight,
            absoluteTolerance = 0.0001f,
        )

        val firstBreak = segments[1] as TimelineSegmentUiModel.ShortBreak
        assertEquals(5, firstBreak.duration)
        assertEquals(
            expected = 5f / totalMinutes,
            actual = firstBreak.weight,
            absoluteTolerance = 0.0001f,
        )

        val lastFocus = segments.last() as TimelineSegmentUiModel.Focus
        assertEquals(25, lastFocus.duration)

        assertEquals(3, segments.count { it is TimelineSegmentUiModel.ShortBreak })
        assertFalse(segments.any { it is TimelineSegmentUiModel.LongBreak })
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
        val totalMinutes = segments.sumOf { it.duration }
        val longBreakCount = segments.count { it is TimelineSegmentUiModel.LongBreak }

        assertEquals(3, longBreakCount) // after 2nd, 4th, and 6th focus
        assertEquals(20, (segments[3] as TimelineSegmentUiModel.LongBreak).duration)
        assertEquals(
            expected = 20f / totalMinutes,
            actual = (segments[3] as TimelineSegmentUiModel.LongBreak).weight,
            absoluteTolerance = 0.0001f,
        )
        assertEquals(20, (segments[7] as TimelineSegmentUiModel.LongBreak).duration)
        assertEquals(
            expected = 20f / totalMinutes,
            actual = (segments[7] as TimelineSegmentUiModel.LongBreak).weight,
            absoluteTolerance = 0.0001f,
        )
        assertEquals(20, (segments.last() as TimelineSegmentUiModel.LongBreak).duration)
        assertEquals(
            expected = 20f / totalMinutes,
            actual = (segments.last() as TimelineSegmentUiModel.LongBreak).weight,
            absoluteTolerance = 0.0001f,
        )
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
