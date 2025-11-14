package com.fakhry.pomodojo.dashboard.ui.mapper

import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

internal actual fun platformThousandsSeparator(): Char {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    val groupingSeparator = formatter.groupingSeparator
    return groupingSeparator.firstOrNull() ?: ','
}
