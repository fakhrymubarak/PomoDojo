package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.ui.theme.ButtonPrimary
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import com.fakhry.pomodojo.ui.theme.TextWhite
import com.fakhry.pomodojo.utils.formatTimerMinutes
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


/**
 * Pomodoro Timer Section
 * Contains the timer display and start button
 */
@Composable
fun PomodoroTimerSection(
    timerMinutes: Int,
    onStartPomodoro: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimerCard(
            timerMinutes = timerMinutes,
            onStartPomodoro = onStartPomodoro,
        )
    }
}

@Composable
private fun TimerCard(
    timerMinutes: Int,
    onStartPomodoro: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Timer visuals with decorative dots
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            TimerDecorationCanvas()

            // Timer text
            Text(
                text = formatTimerMinutes(timerMinutes),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Timer set to $timerMinutes minutes"
                },
                textAlign = TextAlign.Center,
            )
        }

        // Start button
        Button(
            onClick = onStartPomodoro,
            modifier = Modifier.semantics { contentDescription = "Start Pomodoro" },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = TextWhite,
            ),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(
                text = "Start Pomodoro",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}



@Composable
private fun TimerDecorationCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
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

@Preview
@Composable
fun PomodoroTimerSectionPreview() {
    PomoDojoTheme {
        PomodoroTimerSection(
            timerMinutes = 25,

        )
    }
}
