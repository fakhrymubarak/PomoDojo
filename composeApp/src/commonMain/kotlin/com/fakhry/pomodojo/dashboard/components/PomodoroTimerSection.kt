package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.focus.ui.PhaseType
import com.fakhry.pomodojo.ui.components.PomodoroTimerDecoration
import com.fakhry.pomodojo.ui.theme.LongBreakHighlight
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pomodoro Timer Section
 * Contains the timer display and start button
 */
@Composable
fun PomodoroTimerSection(
    phaseType: PhaseType = PhaseType.FOCUS,
    formattedTime: String = "00:00",
    progress: Float = 0.5f,
) {
    val color = when (phaseType) {
        PhaseType.FOCUS -> Secondary
        PhaseType.SHORT_BREAK -> Primary
        PhaseType.LONG_BREAK -> LongBreakHighlight
    }

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        PomodoroTimerDecoration(
            progressColor = color,
            progress = progress,
        )

        // Timer text
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
fun PomodoroTimerSectionFocusPreview() {
    PomoDojoTheme {
        Column {
            PomodoroTimerSection(
                phaseType = PhaseType.FOCUS,
            )
        }
    }
}

@Preview
@Composable
fun PomodoroTimerSectionBreakPreview() {
    PomoDojoTheme {
        Column {
            PomodoroTimerSection(
                phaseType = PhaseType.SHORT_BREAK,
            )
        }
    }
}

@Preview
@Composable
fun PomodoroTimerSectionLongBreakPreview() {
    PomoDojoTheme {
        Column {
            PomodoroTimerSection(
                phaseType = PhaseType.LONG_BREAK,
            )
        }
    }
}
