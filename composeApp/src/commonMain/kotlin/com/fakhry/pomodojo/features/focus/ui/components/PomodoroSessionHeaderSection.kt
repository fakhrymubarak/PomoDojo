package com.fakhry.pomodojo.features.focus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.core.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.features.focus.ui.focusPhaseLabel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_session_header_cycle_count
import com.fakhry.pomodojo.generated.resources.focus_session_header_title
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PomodoroSessionHeaderSection(timerType: TimerType, cycleNumber: Int, totalCycle: Int) {
    BgHeaderCanvas {
        Column(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.focus_session_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )

            val phaseLabel = focusPhaseLabel(timerType)
            val cycleLabel = stringResource(
                Res.string.focus_session_header_cycle_count,
                cycleNumber,
                totalCycle,
            )
            val subtitle = "$phaseLabel • $cycleLabel"
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PomodoroSessionHeaderSectionPreview() {
    PomoDojoTheme {
        PomodoroSessionHeaderSection(TimerType.FOCUS, 2, 4)
    }
}
