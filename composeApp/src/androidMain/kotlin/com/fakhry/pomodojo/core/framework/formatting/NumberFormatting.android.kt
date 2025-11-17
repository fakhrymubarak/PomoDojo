package com.fakhry.pomodojo.core.framework.formatting

import java.text.DecimalFormatSymbols

internal actual fun platformThousandsSeparator(): Char =
    DecimalFormatSymbols.getInstance().groupingSeparator
