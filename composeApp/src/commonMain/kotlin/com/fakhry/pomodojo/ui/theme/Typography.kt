package com.fakhry.pomodojo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

val PomoDojoTypography = Typography(
    // Headline 1 - Screen titles, major headings
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // TODO: Replace with Plus Jakarta Sans when font is added
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    // Headline 2 - Section titles, dialog titles
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),

    // Large display - Timer display
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
    ),

    // Title Medium - Card titles
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Label Large - Button text (TextBold12)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // Body Medium - Regular body text (Regular12)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // Label Small - Small labels, graph labels (Regular8)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 12.sp,
    ),
)
