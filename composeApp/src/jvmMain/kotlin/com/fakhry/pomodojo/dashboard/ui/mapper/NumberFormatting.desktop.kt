package com.fakhry.pomodojo.dashboard.ui.mapper

import java.text.DecimalFormatSymbols

internal actual fun platformThousandsSeparator(): Char =
    DecimalFormatSymbols.getInstance().groupingSeparator
