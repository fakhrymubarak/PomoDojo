package com.fakhry.pomodojo.utils

import kotlin.math.ceil

/**
 * Formats a duration in milliseconds into a "MM:SS" string.
 *
 * For example:
 * - `25000L` (25 seconds) becomes "00:25"
 * - `125001L` (125.001 seconds) is rounded up to 126 seconds, becoming "02:06"
 * - `59999L` (59.999 seconds) is rounded up to 60 seconds, becoming "01:00"
 * - `-5000L` becomes "00:00"
 *
 * @return A [String] representing the duration in MM:SS format.
 * @receiver The duration in milliseconds.
 * @see formatMmSs
 */
fun Long.formatDurationMillis(): String {
    val totalSeconds = ceil(this / 1_000.0).toLong().coerceAtLeast(0L)
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return formatMmSs(m.toInt(), s.toInt())
}