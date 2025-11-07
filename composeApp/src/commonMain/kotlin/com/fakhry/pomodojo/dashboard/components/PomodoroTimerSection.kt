package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.pomodoro_timer_content_description
import com.fakhry.pomodojo.generated.resources.pomodoro_timer_start
import com.fakhry.pomodojo.ui.components.PomodoroTimerDecoration
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.utils.formatTimerMinutes
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


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
    val timerContentDescription =
        stringResource(Res.string.pomodoro_timer_content_description, timerMinutes)
    val startLabel = stringResource(Res.string.pomodoro_timer_start)

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
            PomodoroTimerDecoration()

            // Timer text
            Text(
                text = formatTimerMinutes(timerMinutes),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.semantics {
                    contentDescription = timerContentDescription
                },
                textAlign = TextAlign.Center,
            )
        }

        // Start button
        Button(
            onClick = onStartPomodoro,
            modifier = Modifier.semantics { contentDescription = startLabel },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(
                text = startLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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
