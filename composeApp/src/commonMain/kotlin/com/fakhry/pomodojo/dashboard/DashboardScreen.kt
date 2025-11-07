package com.fakhry.pomodojo.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.dashboard.components.FocusHistorySection
import com.fakhry.pomodojo.dashboard.components.PomodoroTimerSection
import com.fakhry.pomodojo.dashboard.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.dashboard_header_title
import com.fakhry.pomodojo.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Suppress("NonSkippableComposable")
@Composable
fun DashboardScreen(
    onStartPomodoro: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).fillMaxSize(),
        ) {
            DashboardHeader(onOpenSettings = onOpenSettings)
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
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
                    onSelectYear = viewModel::selectYear,
                )
            }
        }
    }
}


@Composable
private fun DashboardHeader(
    onOpenSettings: () -> Unit = {},
) {
    BgHeaderCanvas {
        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.dashboard_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                ),
            )
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = onOpenSettings,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WavyHeaderPreview() {
    PomoDojoTheme {
        DashboardHeader()
    }
}
