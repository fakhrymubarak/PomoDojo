package com.fakhry.pomodojo.core.utils.date

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun formatTimerMinutes(minutes: Int) = "${minutes.coerceAtLeast(0).toString().padStart(2, '0')}:00"

expect fun formatMmSs(m: Int, s: Int): String

/** Formats this instant as a 24-hour wall-clock `HH:mm` in [timeZone]. */
@OptIn(ExperimentalTime::class)
fun Instant.toClockHhMm(timeZone: TimeZone): String {
    val dateTime = toLocalDateTime(timeZone)
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
