package com.fakhry.pomodojo.core.framework.formatting

import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

internal actual fun platformThousandsSeparator(): Char {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    val groupingSeparator = formatter.groupingSeparator
    return groupingSeparator.firstOrNull() ?: ','
}
