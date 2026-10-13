package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import com.fakhry.pomodojo.core.designsystem.effects.ImmersiveModeEffect
import com.fakhry.pomodojo.core.designsystem.effects.KeepScreenOnEffect
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.features.focus.ui.model.SessionConfirm
import com.fakhry.pomodojo.features.focus.ui.viewmodel.PomodoroSessionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val LANDSCAPE_MIN_ASPECT_RATIO = 0.8f

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("NonSkippableComposable")
@Composable
fun PomodoroSessionScreen(
    onSessionCompleted: (PomodoroCompletionUiState) -> Unit,
    viewModel: PomodoroSessionViewModel = koinViewModel(),
) {
    val alwaysOnDisplay = viewModel.alwaysOnDisplay.collectAsState()

    KeepScreenOnEffect(alwaysOnDisplay.value)
    ImmersiveModeEffect()

    BackHandler {
        viewModel.onBackPressed()
    }

    viewModel.collectSideEffect { sideEffect ->
        handleSessionSideEffect(sideEffect, onSessionCompleted)
    }

    val state = viewModel.collectAsState().value
    val activeSegment = state.activeSegment
    val isTimerRunning = activeSegment.timerStatus == TimerStatusUi.RUNNING

    Box(modifier = Modifier.fillMaxSize()) {
        SessionContent(
            state = state,
            isTimerRunning = isTimerRunning,
            onTogglePause = viewModel::togglePauseResume,
            onEnd = viewModel::onEndClicked,
            onSkip = viewModel::onSkipClicked,
        )

        if (state.awaitingContinue && state.finishedPhaseType != null) {
            PhaseTransitionGate(
                finishedPhase = state.finishedPhaseType,
                nextPhase = state.nextPhaseType,
                nextDurationMs = state.nextPhaseDurationMs,
                onContinue = viewModel::onContinueNextPhase,
                onFinish = viewModel::onConfirmFinish,
            )
        }

        state.pendingConfirm?.let { confirm ->
            SessionConfirmOverlay(
                confirm = confirm,
                onConfirm = {
                    when (confirm) {
                        SessionConfirm.END_SESSION -> viewModel.onConfirmFinish()
                        SessionConfirm.SKIP_BREAK -> viewModel.onConfirmSkip()
                    }
                },
                onDismiss = viewModel::onDismissConfirm,
            )
        }
    }
}

private fun handleSessionSideEffect(
    sideEffect: PomodoroSessionSideEffect,
    onSessionCompleted: (PomodoroCompletionUiState) -> Unit,
) {
    when (sideEffect) {
        is PomodoroSessionSideEffect.OnSessionComplete ->
            onSessionCompleted(sideEffect.completionResult)
    }
}

@Composable
private fun SessionContent(
    state: PomodoroSessionUiState,
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
    onSkip: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Near-square screens (foldables, desktop windows) also use the landscape layout
            if (maxWidth >= maxHeight * LANDSCAPE_MIN_ASPECT_RATIO) {
                LandscapeSessionContent(
                    state = state,
                    isTimerRunning = isTimerRunning,
                    onTogglePause = onTogglePause,
                    onEnd = onEnd,
                    onSkip = onSkip,
                )
            } else {
                PortraitSessionContent(
                    state = state,
                    isTimerRunning = isTimerRunning,
                    onTogglePause = onTogglePause,
                    onEnd = onEnd,
                    onSkip = onSkip,
                )
            }
        }
    }
}
