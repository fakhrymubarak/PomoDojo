package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.runtime.Composable
import com.fakhry.pomodojo.core.designsystem.components.focusPhaseLabel
import com.fakhry.pomodojo.core.designsystem.dialog.DialogAction
import com.fakhry.pomodojo.core.designsystem.dialog.DialogCard
import com.fakhry.pomodojo.core.designsystem.dialog.ModalBackdrop
import com.fakhry.pomodojo.core.designsystem.dialog.PomoModalOverlay
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_continue
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_finish
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_message_format
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_title_format
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import org.jetbrains.compose.resources.stringResource

private const val MILLIS_PER_MINUTE = 60_000L

/**
 * Full-screen phase-transition gate shown between pomodoro phases.
 *
 * A glow tinted by the just-finished phase sweeps in from the screen edges for ~2.5s
 * (not skippable), then the continue/finish dialog is revealed. Rendered through the shared
 * [PomoModalOverlay] edge-glow backdrop — the only overlay in the app that uses this glow, since
 * it marks a phase completing. Covers the session content and blocks interaction with it while
 * open.
 */
@Composable
internal fun PhaseTransitionGate(
    finishedPhase: TimerTypeUi,
    nextPhase: TimerTypeUi?,
    nextDurationMs: Long,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    PomoModalOverlay(
        backdrop = ModalBackdrop.EdgeGlow(
            color = finishedPhase.phaseGlowColor(),
            animateIntro = true,
        ),
        dismissable = false,
    ) {
        GatePhaseDialog(
            finishedPhase = finishedPhase,
            nextPhase = nextPhase,
            nextDurationMs = nextDurationMs,
            onContinue = onContinue,
            onFinish = onFinish,
        )
    }
}

@Composable
private fun GatePhaseDialog(
    finishedPhase: TimerTypeUi,
    nextPhase: TimerTypeUi?,
    nextDurationMs: Long,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    val finishedLabel = focusPhaseLabel(finishedPhase)
    val nextLabel = focusPhaseLabel(nextPhase ?: finishedPhase)
    val nextMinutes = (nextDurationMs / MILLIS_PER_MINUTE).toInt().coerceAtLeast(1)

    DialogCard(
        title = stringResource(Res.string.focus_session_gate_title_format, finishedLabel),
        message = stringResource(
            Res.string.focus_session_gate_message_format,
            nextMinutes,
            nextLabel,
        ),
        confirm = DialogAction(
            text = stringResource(Res.string.focus_session_gate_continue),
            onClick = onContinue,
        ),
        dismiss = DialogAction(
            text = stringResource(Res.string.focus_session_gate_finish),
            onClick = onFinish,
        ),
    )
}
