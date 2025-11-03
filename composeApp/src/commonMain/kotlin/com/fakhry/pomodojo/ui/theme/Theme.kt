package com.fakhry.pomodojo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * PomoDojo color schemes for dark and light themes.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = DarkOnPrimary,
    primaryContainer = Primary,
    onPrimaryContainer = DarkOnPrimary,

    secondary = Secondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = Secondary,
    onSecondaryContainer = DarkOnPrimary,

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    tertiary = Secondary,
    onTertiary = DarkOnPrimary,

    error = Primary,
    onError = DarkOnPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = LightOnPrimary,
    primaryContainer = Primary,
    onPrimaryContainer = LightOnPrimary,

    secondary = Secondary,
    onSecondary = LightOnPrimary,
    secondaryContainer = Secondary,
    onSecondaryContainer = LightOnPrimary,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
    tertiary = Secondary,
    onTertiary = LightOnPrimary,

    error = Primary,
    onError = LightOnPrimary,
)

/**
 * PomoDojo Theme
 *
 * Wraps MaterialTheme with app specific typography and colors.
 */
@Composable
fun PomoDojoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = pomoDojoTypography(),
        content = content
    )
}
