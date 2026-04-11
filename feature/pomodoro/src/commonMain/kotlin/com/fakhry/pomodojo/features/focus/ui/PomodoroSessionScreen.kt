package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import com.fakhry.pomodojo.core.designsystem.effects.KeepScreenOnEffect
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.features.focus.ui.viewmodel.PomodoroSessionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("NonSkippableComposable")
@Composable
fun PomodoroSessionScreen(
    onSessionCompleted: (PomodoroCompletionUiState) -> Unit,
    viewModel: PomodoroSessionViewModel = koinViewModel(),
) {
    var showEndDialog by rememberSaveable { mutableStateOf(false) }
    val alwaysOnDisplay = viewModel.alwaysOnDisplay.collectAsState()

    KeepScreenOnEffect(alwaysOnDisplay.value)

    BackHandler {
        viewModel.onEndClicked()
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PomodoroSessionSideEffect.OnSessionComplete -> {
                onSessionCompleted(sideEffect.completionResult)
            }

            is PomodoroSessionSideEffect.ShowEndSessionDialog -> showEndDialog = sideEffect.isShown
        }
    }

    val state = viewModel.collectAsState().value
    val activeSegment = state.activeSegment
    val isTimerRunning = activeSegment.timerStatus == TimerStatusUi.RUNNING

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                LandscapeSessionContent(
                    state = state,
                    isTimerRunning = isTimerRunning,
                    onTogglePause = viewModel::togglePauseResume,
                    onEnd = viewModel::onEndClicked,
                )
            } else {
                PortraitSessionContent(
                    state = state,
                    isTimerRunning = isTimerRunning,
                    onTogglePause = viewModel::togglePauseResume,
                    onEnd = viewModel::onEndClicked,
                )
            }
        }
    }

    if (showEndDialog) {
        FocusConfirmDialog(
            onConfirmFinish = viewModel::onConfirmFinish,
            onDismiss = viewModel::onDismissConfirmEnd,
        )
    }
}
