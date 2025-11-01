package com.fakhry.pomodojo.utils

fun formatTimerMinutes(minutes: Int) =
    "${minutes.coerceAtLeast(0).toString().padStart(2, '0')}:00"
