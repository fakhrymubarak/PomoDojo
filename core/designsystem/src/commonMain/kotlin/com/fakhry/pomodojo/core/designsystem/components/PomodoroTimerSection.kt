package com.fakhry.pomodojo.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_phase_break
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_phase_focus
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_phase_long_break
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.theme.LongBreakHighlight
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import com.fakhry.pomodojo.core.designsystem.theme.Primary
import com.fakhry.pomodojo.core.designsystem.theme.Secondary
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pomodoro Timer Section
 * Contains the timer display and start button
 */
@Composable
fun PomodoroTimerSection(
    segmentType: TimerTypeUi = TimerTypeUi.FOCUS,
    formattedTime: String = "00:00",
    progress: Float = 0.5f,
) {
    val color = when (segmentType) {
        TimerTypeUi.FOCUS -> Secondary
        TimerTypeUi.SHORT_BREAK -> Primary
        TimerTypeUi.LONG_BREAK -> LongBreakHighlight
    }

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        PomodoroTimerDecoration(
            progressColor = color,
            progress = progress,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                textAlign = TextAlign.Center,
            )
            FocusPhaseChip(phase = segmentType, color = color)
        }
    }
}

@Composable
private fun FocusPhaseChip(phase: TimerTypeUi, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
    ) {
        Text(
            text = focusPhaseLabel(phase),
            style = MaterialTheme.typography.labelLarge.copy(
                color = color,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Preview
@Composable
fun PomodoroTimerSectionFocusPreview() {
    PomoDojoTheme {
        Column {
            PomodoroTimerSection(
                segmentType = TimerTypeUi.FOCUS,
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
                segmentType = TimerTypeUi.SHORT_BREAK,
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
                segmentType = TimerTypeUi.LONG_BREAK,
            )
        }
    }
}

@Composable
fun focusPhaseLabel(phase: TimerTypeUi) = when (phase) {
    TimerTypeUi.FOCUS -> stringResource(Res.string.focus_session_phase_focus)
    TimerTypeUi.SHORT_BREAK -> stringResource(Res.string.focus_session_phase_break)
    TimerTypeUi.LONG_BREAK -> stringResource(Res.string.focus_session_phase_long_break)
}
