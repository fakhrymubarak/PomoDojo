package com.fakhry.pomodojo.preferences.domain.usecase

object PreferencesValidator {
    private val repeatRange = 2..8
    private val focusOptions = setOf(10, 25, 50)
    private val breakOptions = setOf(2, 5, 10)
    private val longBreakAfterOptions = setOf(2, 4, 6)
    private val longBreakDurations = setOf(4, 10, 20)

    fun isValidRepeatCount(value: Int): Boolean = value in repeatRange
    fun isValidFocusMinutes(value: Int): Boolean = value in focusOptions
    fun isValidBreakMinutes(value: Int): Boolean = value in breakOptions
    fun isValidLongBreakAfter(value: Int): Boolean = value in longBreakAfterOptions
    fun isValidLongBreakMinutes(value: Int): Boolean = value in longBreakDurations
}