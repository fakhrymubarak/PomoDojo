package com.fakhry.pomodojo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Decorative ring + orbiting dots used by pomodoro timers across the app.
 */
@Composable
fun PomodoroTimerDecoration(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(x = size.width / 2f, y = size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val ringStrokeWidth = 8.dp.toPx()
        val ringRadius = (maxRadius - 30.dp.toPx()).coerceAtLeast(ringStrokeWidth / 2f)

        // Outer ring stroke
        drawCircle(
            color = Secondary,
            radius = ringRadius,
            center = center,
            style = Stroke(width = ringStrokeWidth),
        )

        val dotRadius = 3.dp.toPx()
        val dotCount = 12
        val orbitRadius = (ringRadius + 16.dp.toPx()).coerceAtMost(maxRadius - dotRadius)

        repeat(dotCount) { index ->
            val angle = (2 * PI * index / dotCount) - (PI / 2)
            val dotCenter = Offset(
                x = center.x + orbitRadius * cos(angle).toFloat(),
                y = center.y + orbitRadius * sin(angle).toFloat(),
            )
            drawCircle(
                color = Primary,
                radius = dotRadius,
                center = dotCenter,
            )
        }
    }
}
