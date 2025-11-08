package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
) {
    val timerContentDescription =
        stringResource(Res.string.pomodoro_timer_content_description, timerMinutes)
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
}

@Preview
@Composable
fun PomodoroTimerSectionPreview() {
    PomoDojoTheme {
        Column {
            PomodoroTimerSection(
                timerMinutes = 25,
            )
        }
    }
}
