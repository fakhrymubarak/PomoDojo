package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.launch

private const val ENTRANCE_MILLIS = 600
private const val BREATH_MILLIS = 1500
private const val BREATH_LOW = 0.7f
private const val BREATH_HIGH = 1f
private const val SCRIM_ALPHA = 0.35f
private const val EDGE_ALPHA = 0.9f
private const val EDGE_THICKNESS_FRACTION = 0.22f

/**
 * A full-screen dimming scrim with a [glowColor]-tinted glow bleeding in from all four edges, with
 * [content] centered on top. Backs the pomodoro phase-transition gate — the only overlay in the app
 * that uses this glow, since it marks a phase completing.
 *
 * Three stacked layers, bottom to top: a constant dim scrim, the breathing edge glow, and
 * [content]. On appear the scrim and content fade in over ~600ms while the glow fades in and then
 * breathes ([BREATH_HIGH] <-> [BREATH_LOW]) forever. The scrim holds steady so the background dim
 * does not flicker with the glow.
 */
@Composable
fun EdgeGlowOverlay(
    glowColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    // Drives the scrim + content fade-in; holds at 1f once the entrance completes.
    val entrance = remember { Animatable(0f) }
    // Drives the edge glow: fades in, then breathes forever.
    val glow = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            entrance.animateTo(1f, tween(ENTRANCE_MILLIS, easing = FastOutSlowInEasing))
        }
        glow.animateTo(BREATH_HIGH, tween(ENTRANCE_MILLIS, easing = FastOutSlowInEasing))
        while (true) {
            glow.animateTo(BREATH_LOW, tween(BREATH_MILLIS, easing = FastOutSlowInEasing))
            glow.animateTo(BREATH_HIGH, tween(BREATH_MILLIS, easing = FastOutSlowInEasing))
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Bottom layer: constant dim scrim (fades in, then steady).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA * entrance.value)),
        )

        // Middle layer: breathing edge glow.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val intensity = glow.value
            if (intensity > 0f) drawEdgeGlow(glowColor, intensity)
        }

        // Top layer: the modal content.
        Box(modifier = Modifier.alpha(entrance.value)) {
            content()
        }
    }
}

/** Draws a phase-colored glow bleeding in from all four edges. */
private fun DrawScope.drawEdgeGlow(glowColor: Color, glow: Float) {
    val thickness = size.minDimension * EDGE_THICKNESS_FRACTION
    val edgeColor = glowColor.copy(alpha = EDGE_ALPHA * glow)

    // Top edge
    drawRect(
        brush = Brush.verticalGradient(listOf(edgeColor, Color.Transparent), 0f, thickness),
        topLeft = Offset.Zero,
        size = Size(size.width, thickness),
    )
    // Bottom edge
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, edgeColor),
            startY = size.height - thickness,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height - thickness),
        size = Size(size.width, thickness),
    )
    // Left edge
    drawRect(
        brush = Brush.horizontalGradient(listOf(edgeColor, Color.Transparent), 0f, thickness),
        topLeft = Offset.Zero,
        size = Size(thickness, size.height),
    )
    // Right edge
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, edgeColor),
            startX = size.width - thickness,
            endX = size.width,
        ),
        topLeft = Offset(size.width - thickness, 0f),
        size = Size(thickness, size.height),
    )
}
