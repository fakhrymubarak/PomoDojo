package com.fakhry.pomodojo.dashboard.ui.mapper

import platform.Foundation.NSLocale

internal actual fun platformThousandsSeparator(): Char {
    val groupingSeparator = NSLocale.currentLocale.groupingSeparator
    return groupingSeparator?.firstOrNull() ?: ','
}
