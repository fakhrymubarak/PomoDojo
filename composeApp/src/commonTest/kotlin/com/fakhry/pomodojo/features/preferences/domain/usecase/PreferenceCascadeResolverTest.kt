package com.fakhry.pomodojo.features.preferences.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class PreferenceCascadeResolverTest {
    private val cascadeResolver = PreferenceCascadeResolver()

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
}
