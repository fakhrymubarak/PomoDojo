package com.fakhry.pomodojo.core.utils.date

actual fun formatMmSs(m: Int, s: Int) =
    "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
