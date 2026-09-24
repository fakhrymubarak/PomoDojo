package com.fakhry.pomodojo.core.utils.date

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DateUtilsTest {
    @Test
    fun `toClockHhMm formats epoch start as midnight`() {
        assertEquals("00:00", Instant.fromEpochMilliseconds(0L).toClockHhMm(TimeZone.UTC))
    }

    @Test
    fun `toClockHhMm zero-pads hours and minutes`() {
        // 09:05 UTC -> 9h5m = 32_700 seconds
        assertEquals("09:05", Instant.fromEpochMilliseconds(32_700_000L).toClockHhMm(TimeZone.UTC))
    }

    @Test
    fun `toClockHhMm formats afternoon in 24-hour clock`() {
        // 17:30 UTC -> 17h30m = 63_000 seconds
        assertEquals("17:30", Instant.fromEpochMilliseconds(63_000_000L).toClockHhMm(TimeZone.UTC))
    }
}
