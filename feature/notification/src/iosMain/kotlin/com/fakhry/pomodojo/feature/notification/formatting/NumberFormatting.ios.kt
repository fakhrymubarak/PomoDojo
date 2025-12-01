package com.fakhry.pomodojo.feature.notification.formatting

import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun platformThousandsSeparator(): Char {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    val groupingSeparator = formatter.groupingSeparator
    return groupingSeparator.firstOrNull() ?: ','
}
