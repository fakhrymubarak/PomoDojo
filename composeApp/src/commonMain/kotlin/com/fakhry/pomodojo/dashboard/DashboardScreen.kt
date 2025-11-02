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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.dashboard.components.FocusHistorySection
import com.fakhry.pomodojo.dashboard.components.PomodoroTimerSection
import com.fakhry.pomodojo.dashboard.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.dashboard_header_content_description
import com.fakhry.pomodojo.generated.resources.dashboard_header_title
import com.fakhry.pomodojo.generated.resources.dashboard_open_settings_content_description
import com.fakhry.pomodojo.ui.theme.DarkBackground
import com.fakhry.pomodojo.ui.theme.Secondary
import com.fakhry.pomodojo.ui.theme.TextWhite
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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
                    onSelectYear = viewModel::selectYear,
                )
            }
        }
    }
}

@Composable
private fun WavyHeader(
    onOpenSettings: () -> Unit,
) {
    val headerTitle = stringResource(Res.string.dashboard_header_title)
    val headerContentDescription = stringResource(Res.string.dashboard_header_content_description)
    val openSettingsContentDescription =
        stringResource(Res.string.dashboard_open_settings_content_description)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Secondary)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                ),
                modifier = Modifier.semantics { contentDescription = headerContentDescription },
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.semantics {
                    contentDescription = openSettingsContentDescription
                },
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
