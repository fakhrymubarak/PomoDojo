package com.fakhry.pomodojo.core.utils.date

fun formatTimerMinutes(minutes: Int) = "${minutes.coerceAtLeast(0).toString().padStart(2, '0')}:00"

expect fun formatMmSs(m: Int, s: Int): String
