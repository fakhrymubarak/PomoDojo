package com.fakhry.pomodojo.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.dashboard.components.FocusHistorySection
import com.fakhry.pomodojo.dashboard.components.PomodoroTimerSection
import com.fakhry.pomodojo.dashboard.model.ContributionCell
import com.fakhry.pomodojo.dashboard.model.DashboardState
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.ui.theme.DarkBackground
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.SecondaryGreen
import com.fakhry.pomodojo.ui.theme.TextWhite
import com.fakhry.pomodojo.utils.ensureYearCells
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DashboardScreen(
    state: DashboardState,
    modifier: Modifier = Modifier,
    onStartPomodoro: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectYear: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize(),
        ) {
            WavyHeader(onOpenSettings = onOpenSettings)
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                PomodoroTimerSection(
                    timerMinutes = state.timerMinutes,
                    onStartPomodoro = onStartPomodoro,
                )

                // Focus History Section
                FocusHistorySection(
                    totalMinutes = state.focusMinutesThisYear,
                    selectedYear = state.selectedYear,
                    availableYears = state.availableYears,
                    cells = state.cells,
                    onSelectYear = onSelectYear,
                )
            }
        }
    }
}

@Composable
private fun WavyHeader(
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryGreen)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PomoDojo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                ),
                modifier = Modifier.semantics { contentDescription = "PomoDojo Dashboard" },
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.semantics { contentDescription = "Open settings" },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = TextWhite,
                )
            }
        }
    }
}


// ============================================================================
// FOCUS HISTORY SECTION
// ============================================================================


@Preview
@Composable
private fun DashboardPreview() {
    val previewState = previewDashboardState.copy(
        cells = ensureYearCells(
            previewDashboardState.selectedYear,
            generatePreviewCells(previewDashboardState.selectedYear),
        ),
    )
    PomoDojoTheme {
        DashboardScreen(
            state = previewState,
            onStartPomodoro = {},
            onOpenSettings = {},
            onSelectYear = {},
        )
    }
}

private fun generatePreviewCells(year: Int): List<ContributionCell> {
    val start = LocalDate(year, 1, 1)
    val result = mutableListOf<ContributionCell>()
    var cursor = start
    var bucket = 0
    while (bucket < 35) { // generate enough days for preview variety
        val totalMinutes = when (bucket % 6) {
            0 -> 0
            1 -> 15
            2 -> 25
            3 -> 50
            4 -> 75
            else -> 85
        }
        result += ContributionCell(
            date = cursor.toString(),
            totalMinutes = totalMinutes,
            intensityLevel = intensityLevelForMinutes(totalMinutes),
        )
        cursor = cursor.plus(DatePeriod(days = 1))
        bucket += 1
    }
    return result
}
