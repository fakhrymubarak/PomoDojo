package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val GLOW_IN_MILLIS = 700
private const val GLOW_DIP_MILLIS = 1100
private const val GLOW_SETTLE_MILLIS = 700
private const val GLOW_FAST_SETTLE_MILLIS = 320
private const val GLOW_PEAK = 1f
private const val GLOW_DIP = 0.55f
private const val GLOW_SETTLE = 0.85f
private const val SCRIM_ALPHA = 0.35f
private const val EDGE_ALPHA = 0.9f
private const val EDGE_THICKNESS_FRACTION = 0.22f

/**
 * A full-screen dimming scrim with a [glowColor]-tinted glow bleeding in from all four edges,
 * with [content] centered on top. Extracted from the pomodoro phase-transition gate so the same
 * glow can back both the gate and confirm modals.
 *
 * When [animateIntro] is true the glow sweeps in (peak -> dip -> settle, ~2.5s) and [content] is
 * revealed only after the sweep completes. When false the glow settles quickly and [content] is
 * shown immediately.
 */
@Composable
fun EdgeGlowOverlay(
    glowColor: Color,
    animateIntro: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    var contentVisible by remember { mutableStateOf(!animateIntro) }
    val intensity = remember { Animatable(if (animateIntro) 0f else GLOW_SETTLE) }

    LaunchedEffect(animateIntro) {
        if (animateIntro) {
            intensity.snapTo(0f)
            intensity.animateTo(GLOW_PEAK, tween(GLOW_IN_MILLIS, easing = FastOutSlowInEasing))
            intensity.animateTo(GLOW_DIP, tween(GLOW_DIP_MILLIS, easing = LinearEasing))
            intensity.animateTo(GLOW_SETTLE, tween(GLOW_SETTLE_MILLIS, easing = LinearEasing))
            contentVisible = true
        } else {
            intensity.snapTo(0f)
            intensity.animateTo(
                GLOW_SETTLE,
                tween(GLOW_FAST_SETTLE_MILLIS, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glow = intensity.value
            if (glow > 0f) drawEdgeGlow(glowColor, glow)
        }

        if (contentVisible) content()
    }
}

/** Draws a dimming scrim plus a phase-colored glow bleeding in from all four edges. */
private fun DrawScope.drawEdgeGlow(glowColor: Color, glow: Float) {
    val thickness = size.minDimension * EDGE_THICKNESS_FRACTION
    val edgeColor = glowColor.copy(alpha = EDGE_ALPHA * glow)

    drawRect(color = Color.Black.copy(alpha = SCRIM_ALPHA * glow))

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
