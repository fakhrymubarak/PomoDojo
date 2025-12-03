package com.fakhry.pomodojo.core.notification.formatting

import java.text.DecimalFormatSymbols

actual fun platformThousandsSeparator(): Char = DecimalFormatSymbols.getInstance().groupingSeparator
