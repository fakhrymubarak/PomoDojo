package com.fakhry.pomodojo.utils

actual fun formatMmSs(
    m: Int,
    s: Int,
) = "%02d:%02d".format(m, s)
