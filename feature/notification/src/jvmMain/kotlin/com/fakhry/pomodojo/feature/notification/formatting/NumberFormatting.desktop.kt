package com.fakhry.pomodojo.feature.notification.formatting

import java.text.DecimalFormatSymbols

actual fun platformThousandsSeparator(): Char = DecimalFormatSymbols.getInstance().groupingSeparator
