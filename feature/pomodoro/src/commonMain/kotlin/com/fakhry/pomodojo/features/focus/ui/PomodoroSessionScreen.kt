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
import com.fakhry.pomodojo.core.designsystem.dialog.ConfirmKind
import com.fakhry.pomodojo.core.designsystem.dialog.DialogHandler
import com.fakhry.pomodojo.core.designsystem.dialog.DialogState
import com.fakhry.pomodojo.core.designsystem.dialog.DialogType
import com.fakhry.pomodojo.core.designsystem.dialog.rememberDialogState
import com.fakhry.pomodojo.core.designsystem.effects.ImmersiveModeEffect
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
    val dialogs = rememberDialogState()
    val alwaysOnDisplay = viewModel.alwaysOnDisplay.collectAsState()

    KeepScreenOnEffect(alwaysOnDisplay.value)
    ImmersiveModeEffect()

    BackHandler {
        viewModel.onEndClicked()
    }

    viewModel.collectSideEffect { sideEffect ->
        handleSessionSideEffect(sideEffect, dialogs, onSessionCompleted)
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

        DialogHandler(
            state = dialogs,
            onConfirm = { kind -> handleDialogConfirm(kind, viewModel) },
            onDismiss = { handleDialogDismiss(dialogs, viewModel) },
        )
    }
}

private fun handleSessionSideEffect(
    sideEffect: PomodoroSessionSideEffect,
    dialogs: DialogState,
    onSessionCompleted: (PomodoroCompletionUiState) -> Unit,
) {
    when (sideEffect) {
        is PomodoroSessionSideEffect.OnSessionComplete ->
            onSessionCompleted(sideEffect.completionResult)

        is PomodoroSessionSideEffect.ShowEndSessionDialog ->
            dialogs.toggleConfirm(ConfirmKind.END_FOCUS_SESSION, sideEffect.isShown)

        is PomodoroSessionSideEffect.ShowSkipBreakDialog ->
            dialogs.toggleConfirm(ConfirmKind.SKIP_BREAK, sideEffect.isShown)
    }
}

private fun DialogState.toggleConfirm(kind: ConfirmKind, isShown: Boolean) {
    if (isShown) showDialog(DialogType.Confirm(kind)) else hideDialog()
}

private fun handleDialogConfirm(kind: ConfirmKind, viewModel: PomodoroSessionViewModel) {
    when (kind) {
        ConfirmKind.END_FOCUS_SESSION -> viewModel.onConfirmFinish()
        ConfirmKind.SKIP_BREAK -> viewModel.onConfirmSkip()
    }
}

private fun handleDialogDismiss(dialogs: DialogState, viewModel: PomodoroSessionViewModel) {
    when ((dialogs.current as? DialogType.Confirm)?.kind) {
        ConfirmKind.END_FOCUS_SESSION -> viewModel.onDismissConfirmEnd()
        ConfirmKind.SKIP_BREAK -> viewModel.onDismissConfirmSkip()
        null -> dialogs.hideDialog()
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
            if (maxWidth > maxHeight) {
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
