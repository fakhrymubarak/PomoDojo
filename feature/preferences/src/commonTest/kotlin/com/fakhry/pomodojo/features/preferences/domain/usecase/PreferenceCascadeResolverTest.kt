package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `focus cascade handles short focus option`() {
        val cascade = cascadeResolver.resolveForFocus(minutes = 10)

        assertEquals(2, cascade.breakMinutes)
        assertEquals(6, cascade.longBreakAfterCount)
        assertEquals(4, cascade.longBreakMinutes)
    }

    @Test
    fun `focus cascade handles classic focus duration`() {
        val cascade = cascadeResolver.resolveForFocus(minutes = 25)

        assertEquals(5, cascade.breakMinutes)
        assertEquals(4, cascade.longBreakAfterCount)
        assertEquals(10, cascade.longBreakMinutes)
    }

    @Test
    fun `break cascade handles standard options`() {
        val medium = cascadeResolver.resolveForBreak(minutes = 5)
        val long = cascadeResolver.resolveForBreak(minutes = 10)

        assertEquals(4, medium.longBreakAfterCount)
        assertEquals(10, medium.longBreakMinutes)

        assertEquals(2, long.longBreakAfterCount)
        assertEquals(20, long.longBreakMinutes)
    }

    @Test
    fun `focus cascade throws for unsupported durations`() {
        assertFailsWith<IllegalArgumentException> {
            cascadeResolver.resolveForFocus(minutes = 99)
        }
    }

    @Test
    fun `break cascade throws for unsupported durations`() {
        assertFailsWith<IllegalArgumentException> {
            cascadeResolver.resolveForBreak(minutes = 3)
        }
    }
}
