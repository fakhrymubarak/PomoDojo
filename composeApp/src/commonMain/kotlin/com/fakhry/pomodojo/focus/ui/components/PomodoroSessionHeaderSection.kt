package com.fakhry.pomodojo.focus.ui.components

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
import com.fakhry.pomodojo.focus.ui.PhaseTimerStatus
import com.fakhry.pomodojo.focus.ui.PhaseType
import com.fakhry.pomodojo.focus.ui.PhaseUi
import com.fakhry.pomodojo.focus.ui.PomodoroSessionUiState
import com.fakhry.pomodojo.focus.ui.focusPhaseLabel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_session_header_cycle_count
import com.fakhry.pomodojo.generated.resources.focus_session_header_title
import com.fakhry.pomodojo.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PomodoroSessionHeaderSection(state: PomodoroSessionUiState) {
    val activePhase = state.activePhase
    BgHeaderCanvas {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.focus_session_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            val subtitle = state.let {
                val phaseLabel = focusPhaseLabel(activePhase.type)
                val cycleLabel = stringResource(
                    Res.string.focus_session_header_cycle_count,
                    activePhase.cycleNumber,
                    it.totalCycle,
                )
                "$phaseLabel • $cycleLabel"
            }
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
    val state = PomodoroSessionUiState(
        phases = persistentListOf(
            PhaseUi(
                type = PhaseType.FOCUS,
                cycleNumber = 1,
                timerStatus = PhaseTimerStatus.RUNNING
            )
        ),
        totalCycle = 4
    )

    PomoDojoTheme {
        PomodoroSessionHeaderSection(state)
    }
}
