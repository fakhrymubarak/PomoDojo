package com.fakhry.pomodojo.core.utils.formatting

import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun platformThousandsSeparator(): Char {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    val separator = formatter.groupingSeparator ?: ","
    return separator.firstOrNull() ?: ','
}
