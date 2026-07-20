package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.DialogProperties
import com.fakhry.pomodojo.core.designsystem.components.focusPhaseLabel
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_continue
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_finish
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_message_format
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_gate_title_format
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.theme.LongBreakHighlight
import com.fakhry.pomodojo.core.designsystem.theme.Primary
import com.fakhry.pomodojo.core.designsystem.theme.Secondary
import org.jetbrains.compose.resources.stringResource

private const val GLOW_IN_MILLIS = 700
private const val GLOW_DIP_MILLIS = 1100
private const val GLOW_SETTLE_MILLIS = 700
private const val GLOW_PEAK = 1f
private const val GLOW_DIP = 0.55f
private const val GLOW_SETTLE = 0.85f
private const val SCRIM_ALPHA = 0.35f
private const val EDGE_ALPHA = 0.9f
private const val EDGE_THICKNESS_FRACTION = 0.22f
private const val MILLIS_PER_MINUTE = 60_000L

/**
 * Full-screen phase-transition gate shown between pomodoro phases.
 *
 * A glow tinted by the just-finished phase sweeps in from the screen edges for ~2.5s
 * (not skippable), then the continue/finish dialog fades in. Covers the session content
 * and blocks interaction with it while open. Works in both portrait and landscape.
 */
@Composable
internal fun PhaseTransitionGate(
    finishedPhase: TimerTypeUi,
    nextPhase: TimerTypeUi?,
    nextDurationMs: Long,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    val glowColor = finishedPhase.phaseColor()
    var showDialog by remember(finishedPhase) { mutableStateOf(false) }
    val intensity = remember(finishedPhase) { Animatable(0f) }

    LaunchedEffect(finishedPhase) {
        intensity.animateTo(GLOW_PEAK, tween(GLOW_IN_MILLIS, easing = FastOutSlowInEasing))
        intensity.animateTo(GLOW_DIP, tween(GLOW_DIP_MILLIS, easing = LinearEasing))
        intensity.animateTo(GLOW_SETTLE, tween(GLOW_SETTLE_MILLIS, easing = LinearEasing))
        showDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Consume all pointer events so the gate is not skippable and the session
            // controls behind it stay inert while the animation plays.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glow = intensity.value
            if (glow > 0f) drawEdgeGlow(glowColor, glow)
        }

        if (showDialog) {
            GatePhaseDialog(
                finishedPhase = finishedPhase,
                nextPhase = nextPhase,
                nextDurationMs = nextDurationMs,
                onContinue = onContinue,
                onFinish = onFinish,
            )
        }
    }
}

/** Draws a dimming scrim plus a phase-colored glow bleeding in from all four edges. */
private fun DrawScope.drawEdgeGlow(glowColor: Color, glow: Float) {
    val thickness = size.minDimension * EDGE_THICKNESS_FRACTION
    val edgeColor = glowColor.copy(alpha = EDGE_ALPHA * glow)

    drawRect(color = Color.Black.copy(alpha = SCRIM_ALPHA * glow))

    // Top edge
    drawRect(
        brush = Brush.verticalGradient(listOf(edgeColor, Color.Transparent), 0f, thickness),
        topLeft = Offset.Zero,
        size = Size(size.width, thickness),
    )
    // Bottom edge
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, edgeColor),
            startY = size.height - thickness,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height - thickness),
        size = Size(size.width, thickness),
    )
    // Left edge
    drawRect(
        brush = Brush.horizontalGradient(listOf(edgeColor, Color.Transparent), 0f, thickness),
        topLeft = Offset.Zero,
        size = Size(thickness, size.height),
    )
    // Right edge
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, edgeColor),
            startX = size.width - thickness,
            endX = size.width,
        ),
        topLeft = Offset(size.width - thickness, 0f),
        size = Size(thickness, size.height),
    )
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

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(
                text = stringResource(Res.string.focus_session_gate_title_format, finishedLabel),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = stringResource(
                    Res.string.focus_session_gate_message_format,
                    nextMinutes,
                    nextLabel,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(
                    text = stringResource(Res.string.focus_session_gate_continue),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onFinish) {
                Text(
                    text = stringResource(Res.string.focus_session_gate_finish),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

private fun TimerTypeUi.phaseColor(): Color = when (this) {
    TimerTypeUi.FOCUS -> Secondary
    TimerTypeUi.SHORT_BREAK -> Primary
    TimerTypeUi.LONG_BREAK -> LongBreakHighlight
}
