package com.fakhry.pomodojo.core.utils.formatting

import java.text.DecimalFormatSymbols

actual fun platformThousandsSeparator(): Char = DecimalFormatSymbols.getInstance().groupingSeparator
