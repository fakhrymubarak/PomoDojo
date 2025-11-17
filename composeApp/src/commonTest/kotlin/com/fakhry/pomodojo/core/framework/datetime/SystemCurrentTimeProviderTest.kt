package com.fakhry.pomodojo.core.framework.datetime

import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SystemCurrentTimeProviderTest {
    @Test
    fun `now returns non decreasing epoch millis`() {
        val first = SystemCurrentTimeProvider.now()
        val second = SystemCurrentTimeProvider.now()

        assertTrue(first > 0)
        assertTrue(second >= first)
    }

    @Test
    fun `nowInstant closely matches now`() {
        val millisFromInstant = SystemCurrentTimeProvider.nowInstant().toEpochMilliseconds()
        val millisFromNow = SystemCurrentTimeProvider.now()

        val delta = (millisFromInstant - millisFromNow).absoluteValue
        assertTrue(delta < 50, "Expected Instant and millis to differ by < 50ms but was $delta")
    }
}
