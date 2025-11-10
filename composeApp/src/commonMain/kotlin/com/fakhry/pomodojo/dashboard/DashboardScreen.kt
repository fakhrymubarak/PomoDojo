package com.fakhry.pomodojo.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.dashboard.components.DashboardHeader
import com.fakhry.pomodojo.dashboard.components.PomodoroHistorySection
import com.fakhry.pomodojo.dashboard.components.PomodoroTimerSection
import com.fakhry.pomodojo.dashboard.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.pomodoro_timer_start
import com.fakhry.pomodojo.utils.formatTimerMinutes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Suppress("NonSkippableComposable")
@Composable
fun DashboardScreen(
    onStartPomodoro: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = koinInject(),
) {
    val hasActiveSession by viewModel.hasActiveSession.collectAsState()
    val prefState by viewModel.prefState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()

    val scrollState = rememberScrollState()
    val startLabel = stringResource(Res.string.pomodoro_timer_start)

    LaunchedEffect(hasActiveSession) {
        if (hasActiveSession) onStartPomodoro()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DashboardHeader(onOpenSettings = onOpenSettings)
            PomodoroTimerSection(
                formattedTime = formatTimerMinutes(prefState.focusMinutes),
                progress = 1f,
            )

            // Start button
            Button(
                onClick = onStartPomodoro,
                modifier = Modifier.semantics { contentDescription = startLabel },
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
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

            // Focus History Section
            PomodoroHistorySection(
                modifier = Modifier.padding(horizontal = 16.dp),
                historyState = historyState,
                onSelectYear = viewModel::selectYear,
            )
        }
    }
}
