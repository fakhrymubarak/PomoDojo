package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesValidatorTest {

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
