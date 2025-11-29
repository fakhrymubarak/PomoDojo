package com.fakhry.pomodojo.core.utils.primitives

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrimitivesExtTest {
    @Test
    fun `formatDurationMillis rounds up fractional seconds`() {
        assertEquals("02:06", 125_001L.formatDurationMillis())
        assertEquals("00:25", 25_000L.formatDurationMillis())
    }

    @Test
    fun `formatDurationMillis clamps negatives to zero`() {
        assertEquals("00:00", (-5_000L).formatDurationMillis())
    }

    @Test
    fun `toMinutes truncates fractional minutes`() {
        assertEquals(1, 119_999L.toMinutes())
        assertEquals(2, 120_000L.toMinutes())
    }

    @Test
    fun `orFalse returns false for null`() {
        assertFalse(null.orFalse())
        assertTrue(true.orFalse())
    }
}
