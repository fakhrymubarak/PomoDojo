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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.ui.theme.ButtonPrimary
import com.fakhry.pomodojo.ui.theme.DarkCircleBackground
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.SecondaryGreen
import com.fakhry.pomodojo.ui.theme.TextWhite
import com.fakhry.pomodojo.utils.formatTimerMinutes


/**
 * Pomodoro Timer Section
 * Contains the timer display and start button
 */
@Composable
fun PomodoroTimerSection(
    timerMinutes: Int,
    onStartPomodoro: () -> Unit,
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
        // Timer with decorative dots
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Decorative dots scattered around
            DecorativeDots()

            // Circular ring
            Canvas(
                modifier = Modifier.size(220.dp)
            ) {
                drawCircle(
                    color = DarkCircleBackground,
                    radius = size.minDimension / 2,
                )
                drawCircle(
                    color = Primary,
                    radius = size.minDimension / 2,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
            }

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
private fun DecorativeDots() {
    val dots = remember {
        listOf(
            Pair(0.15f, 0.15f) to Primary,
            Pair(0.25f, 0.85f) to SecondaryGreen,
            Pair(0.85f, 0.25f) to SecondaryGreen,
            Pair(0.75f, 0.75f) to Primary,
            Pair(0.1f, 0.5f) to Primary,
            Pair(0.9f, 0.5f) to SecondaryGreen,
            Pair(0.5f, 0.1f) to SecondaryGreen,
            Pair(0.5f, 0.9f) to Primary,
            Pair(0.2f, 0.35f) to SecondaryGreen,
            Pair(0.8f, 0.65f) to Primary,
            Pair(0.35f, 0.2f) to Primary,
            Pair(0.65f, 0.8f) to SecondaryGreen,
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEach { (position, color) ->
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    x = size.width * position.first,
                    y = size.height * position.second
                )
            )
        }
    }
}
