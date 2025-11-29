package com.fakhry.pomodojo.core.framework.formatting

import java.text.DecimalFormatSymbols

actual fun platformThousandsSeparator(): Char = DecimalFormatSymbols.getInstance().groupingSeparator
