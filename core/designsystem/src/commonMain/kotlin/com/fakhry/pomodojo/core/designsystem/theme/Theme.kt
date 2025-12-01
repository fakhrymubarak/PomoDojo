package com.fakhry.pomodojo.core.designsystem.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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
fun PomoDojoTheme(appTheme: String = "dark", content: @Composable () -> Unit) {
    val targetColorScheme = remember(appTheme) {
        when (appTheme) {
            "dark" -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    val animatedColorScheme = animateColorScheme(
        targetScheme = targetColorScheme,
        isLightScheme = appTheme != "dark",
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = pomoDojoTypography(),
        content = content,
    )
}

@Composable
private fun animateColorScheme(targetScheme: ColorScheme, isLightScheme: Boolean): ColorScheme {
    val primary = animateColorAsState(targetScheme.primary, label = "primary").value
    val onPrimary = animateColorAsState(targetScheme.onPrimary, label = "onPrimary").value
    val primaryContainer =
        animateColorAsState(targetScheme.primaryContainer, label = "primaryContainer").value
    val onPrimaryContainer =
        animateColorAsState(targetScheme.onPrimaryContainer, label = "onPrimaryContainer").value
    val inversePrimary =
        animateColorAsState(targetScheme.inversePrimary, label = "inversePrimary").value

    val secondary = animateColorAsState(targetScheme.secondary, label = "secondary").value
    val onSecondary = animateColorAsState(targetScheme.onSecondary, label = "onSecondary").value
    val secondaryContainer =
        animateColorAsState(targetScheme.secondaryContainer, label = "secondaryContainer").value
    val onSecondaryContainer = animateColorAsState(
        targetScheme.onSecondaryContainer,
        label = "onSecondaryContainer",
    ).value

    val tertiary = animateColorAsState(targetScheme.tertiary, label = "tertiary").value
    val onTertiary = animateColorAsState(targetScheme.onTertiary, label = "onTertiary").value
    val tertiaryContainer =
        animateColorAsState(targetScheme.tertiaryContainer, label = "tertiaryContainer").value
    val onTertiaryContainer = animateColorAsState(
        targetScheme.onTertiaryContainer,
        label = "onTertiaryContainer",
    ).value

    val background = animateColorAsState(targetScheme.background, label = "background").value
    val onBackground = animateColorAsState(targetScheme.onBackground, label = "onBackground").value
    val surface = animateColorAsState(targetScheme.surface, label = "surface").value
    val onSurface = animateColorAsState(targetScheme.onSurface, label = "onSurface").value
    val surfaceVariant =
        animateColorAsState(targetScheme.surfaceVariant, label = "surfaceVariant").value
    val onSurfaceVariant =
        animateColorAsState(targetScheme.onSurfaceVariant, label = "onSurfaceVariant").value
    val surfaceTint = animateColorAsState(targetScheme.surfaceTint, label = "surfaceTint").value
    val inverseSurface =
        animateColorAsState(targetScheme.inverseSurface, label = "inverseSurface").value
    val inverseOnSurface =
        animateColorAsState(targetScheme.inverseOnSurface, label = "inverseOnSurface").value

    val outline = animateColorAsState(targetScheme.outline, label = "outline").value
    val outlineVariant =
        animateColorAsState(targetScheme.outlineVariant, label = "outlineVariant").value
    val scrim = animateColorAsState(targetScheme.scrim, label = "scrim").value

    val error = animateColorAsState(targetScheme.error, label = "error").value
    val onError = animateColorAsState(targetScheme.onError, label = "onError").value
    val errorContainer =
        animateColorAsState(targetScheme.errorContainer, label = "errorContainer").value
    val onErrorContainer = animateColorAsState(
        targetScheme.onErrorContainer,
        label = "onErrorContainer",
    ).value

    return if (isLightScheme) {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
        )
    } else {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
        )
    }
}
