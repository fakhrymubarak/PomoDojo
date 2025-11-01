package com.fakhry.pomodojo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * PomoDojo Dark Color Scheme
 * Primary theme for the application
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextWhite,
    primaryContainer = Primary,
    onPrimaryContainer = TextWhite,

    secondary = Secondary,
    onSecondary = TextWhite,
    secondaryContainer = Secondary,
    onSecondaryContainer = TextWhite,

    background = DarkBackground,
    onBackground = TextWhite,

    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkCircleBackground,
    onSurfaceVariant = TextLightGray,

    error = Primary,
    onError = TextWhite,
)

/**
 * PomoDojo Theme
 *
 * Currently only supports dark mode as specified in the design.
 * Light mode can be added in the future.
 */
@Composable
fun PomoDojoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PomoDojoTypography,
        content = content
    )
}
