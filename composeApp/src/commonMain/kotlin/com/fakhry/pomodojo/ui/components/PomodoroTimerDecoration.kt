package com.fakhry.pomodojo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Decorative ring + orbiting dots used by pomodoro timers across the app.
 */
@Composable
fun PomodoroTimerDecoration(modifier: Modifier = Modifier, progressColor: Color, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "focus-timer-progress",
    )
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(x = size.width / 2f, y = size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val strokeWidth = 12.dp.toPx()
        val ringRadius = (maxRadius - 30.dp.toPx()).coerceAtLeast(strokeWidth / 2f)

        drawCircle(
            color = colorScheme.surfaceVariant,
            radius = ringRadius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            center = center,
        )
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360 * animatedProgress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = center - Offset(ringRadius, ringRadius),
            size = Size(ringRadius * 2, ringRadius * 2),
        )

        val dotRadius = 4.dp.toPx()
        val dotCount = 12
        val orbitRadius = (ringRadius + 24.dp.toPx()).coerceAtMost(maxRadius - dotRadius)

        // Outer ring stroke
        repeat(dotCount) { index ->
            val angle = (2 * PI * index / dotCount) - (PI / 2)
            val dotCenter =
                Offset(
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

@Preview
@Composable
fun PomodoroTimerDecorationPreview() {
    PomodoroTimerDecoration(
        progressColor = Secondary,
        progress = 0.5f,
    )
}
