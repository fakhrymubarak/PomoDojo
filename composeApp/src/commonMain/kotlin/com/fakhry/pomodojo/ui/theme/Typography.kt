package com.fakhry.pomodojo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.generated.resources.PlusJakartaSans_Bold
import com.fakhry.pomodojo.generated.resources.PlusJakartaSans_ExtraBold
import com.fakhry.pomodojo.generated.resources.PlusJakartaSans_Regular
import com.fakhry.pomodojo.generated.resources.PlusJakartaSans_SemiBold
import com.fakhry.pomodojo.generated.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * PomoDojo Typography
 *
 * Based on themes-specs.md
 * Font Family: Plus Jakarta Sans (using system default as fallback)
 *
 * Defined styles:
 * - Headline 1: ExtraBold 24pt (screen titles)
 * - Headline 2: ExtraBold 16pt (section titles, dialog titles)
 * - TextBold12: Bold 12pt (button labels, preset options)
 * - Regular12: Regular 12pt (body text, configuration labels)
 * - Regular8: Regular 8pt (small labels, graph axis)
 */
@Composable
fun pomoDojoTypography(): Typography {
    val plusJakartaFamily =
        FontFamily(
            Font(resource = Res.font.PlusJakartaSans_Regular, weight = FontWeight.Normal),
            Font(resource = Res.font.PlusJakartaSans_SemiBold, weight = FontWeight.SemiBold),
            Font(resource = Res.font.PlusJakartaSans_Bold, weight = FontWeight.Bold),
            Font(resource = Res.font.PlusJakartaSans_ExtraBold, weight = FontWeight.ExtraBold),
        )

    return Typography(
        // Headline 1 - Screen titles, major headings
        headlineLarge =
        TextStyle(
            // TODO: Replace with Plus Jakarta Sans when font is added
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        // Headline 2 - Section titles, dialog titles
        headlineMedium =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        // Large display - Timer display
        displayLarge =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 56.sp,
            lineHeight = 64.sp,
        ),
        // Title Medium - Card titles
        titleMedium =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        // Label Large - Button text (TextBold12)
        labelLarge =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        // Body Medium - Regular body text (Regular12)
        bodyMedium =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        // Body Small - Small body text (Regular10)
        bodySmall =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 16.sp,
        ),
        // Label Small - Small labels, graph labels (Regular8)
        labelSmall =
        TextStyle(
            fontFamily = plusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 8.sp,
            lineHeight = 12.sp,
        ),
    )
}
