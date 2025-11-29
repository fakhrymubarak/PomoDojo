package com.fakhry.pomodojo.core.framework.util

import kotlin.math.ceil

internal fun Long.formatDurationMillis(): String {
    val totalSeconds = ceil(this / 1_000.0).toLong().coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val minutesText = minutes.toString().padStart(2, '0')
    val secondsText = seconds.toString().padStart(2, '0')
    return "$minutesText:$secondsText"
}
