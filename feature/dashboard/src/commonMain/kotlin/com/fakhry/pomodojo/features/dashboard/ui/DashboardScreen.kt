package com.fakhry.pomodojo.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.utils.permissions.rememberNotificationPermissionRequester
import com.fakhry.pomodojo.features.dashboard.ui.components.DashboardHeader
import com.fakhry.pomodojo.features.dashboard.ui.components.PomodoroHistorySection
import com.fakhry.pomodojo.features.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.features.focus.ui.components.PomodoroTimerSection
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.pomodoro_timer_start
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Suppress("NonSkippableComposable")
@Composable
fun DashboardScreen(
    onStartPomodoro: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val hasActiveSession by viewModel.hasActiveSession.collectAsState()

    val scrollState = rememberScrollState()
    val startLabel = stringResource(Res.string.pomodoro_timer_start)
    val notificationRequester = rememberNotificationPermissionRequester()
    var pendingPermissionResult by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(hasActiveSession) {
        if (hasActiveSession) onStartPomodoro()
    }

    LaunchedEffect(pendingPermissionResult) {
        pendingPermissionResult ?: return@LaunchedEffect
        pendingPermissionResult = null
        onStartPomodoro()
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
            val formattedTimer by viewModel.formattedTime.collectAsState()
            val historyState by viewModel.historyState.collectAsState()

            DashboardHeader(onOpenSettings = onOpenSettings)
            PomodoroTimerSection(
                formattedTime = formattedTimer,
                progress = 1f,
            )

            // Start button
            Button(
                onClick = {
                    notificationRequester.requestPermission { granted ->
                        pendingPermissionResult = granted
                    }
                },
                modifier = Modifier.semantics { contentDescription = startLabel },
                colors = ButtonDefaults.buttonColors(
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
