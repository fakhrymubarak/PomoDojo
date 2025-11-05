package com.fakhry.pomodojo.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.focus.domain.model.FocusPhase
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_finish
import com.fakhry.pomodojo.generated.resources.focus_session_end_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_pause_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_paused_label
import com.fakhry.pomodojo.generated.resources.focus_session_phase_break
import com.fakhry.pomodojo.generated.resources.focus_session_phase_focus
import com.fakhry.pomodojo.generated.resources.focus_session_phase_long_break
import com.fakhry.pomodojo.generated.resources.focus_session_quote_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_resume_content_description
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Suppress("NonSkippableComposable")
@Composable
fun FocusPomodoroScreen(
    onNavigateBack: () -> Unit,
    onSessionCompleted: () -> Unit,
    viewModel: FocusPomodoroViewModel = koinViewModel(),
) {
    val preferencesRepository: PreferencesRepository = koinInject()
    val preferences by preferencesRepository.preferences.collectAsState(initial = PreferencesDomain())
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is FocusPomodoroUiState.Completed) {
            onSessionCompleted()
        }
    }

    LaunchedEffect(state, preferences) {
        if (state is FocusPomodoroUiState.Loading) {
            viewModel.startFromPreferences(preferences)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val uiState = state) {
            FocusPomodoroUiState.Loading -> FocusLoadingState(onNavigateBack)
            FocusPomodoroUiState.Completing -> FocusLoadingState(onNavigateBack)
            is FocusPomodoroUiState.Error -> FocusErrorState(onNavigateBack, uiState.message)
            is FocusPomodoroUiState.Completed -> FocusCompletedState(onNavigateBack)
            is FocusPomodoroUiState.Active -> FocusActiveState(
                state = uiState,
                onNavigateBack = onNavigateBack,
                onTogglePause = viewModel::togglePauseResume,
                onEnd = viewModel::onEndClicked,
                onDismissConfirm = viewModel::onDismissConfirmEnd,
                onConfirmFinish = viewModel::onConfirmFinish,
                onTick = viewModel::decrementTimer,
            )
        }
    }
}

@Composable
private fun FocusActiveState(
    state: FocusPomodoroUiState.Active,
    onNavigateBack: () -> Unit,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
    onDismissConfirm: () -> Unit,
    onConfirmFinish: () -> Unit,
    onTick: () -> Unit,
) {
    LaunchedEffect(state.timerStatus, state.remainingSeconds) {
        if (state.timerStatus == FocusTimerStatus.RUNNING) {
            delay(1_000)
            onTick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopArcBackground(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            FocusTopBar(onNavigateBack = onNavigateBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                FocusTimerDisplay(
                    formattedTime = state.formattedTime,
                    progress = state.progress,
                    status = state.timerStatus,
                    phase = state.phase,
                )

                FocusSegmentBar(
                    completedSegments = state.completedSegments,
                    totalSegments = state.totalSegments,
                    overallProgress = state.progress,
                )

                FocusQuoteBlock(state)
            }

            FocusControls(
                state = state,
                onTogglePause = onTogglePause,
                onEnd = onEnd,
            )
        }

        if (state.showConfirmEndDialog) {
            FocusConfirmDialog(
                onConfirmFinish = onConfirmFinish,
                onDismiss = onDismissConfirm,
            )
        }
    }
}

@Composable
private fun FocusTimerDisplay(
    formattedTime: String,
    progress: Float,
    status: FocusTimerStatus,
    phase: FocusPhase,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(240.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 16.dp.toPx()
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            FocusPhaseChip(phase = phase)
        }

        AnimatedVisibility(
            visible = status == FocusTimerStatus.PAUSED,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                shape = CircleShape,
                tonalElevation = 4.dp,
            ) {
                Text(
                    text = stringResource(Res.string.focus_session_paused_label),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FocusPhaseChip(phase: FocusPhase) {
    val label = when (phase) {
        FocusPhase.FOCUS -> stringResource(Res.string.focus_session_phase_focus)
        FocusPhase.SHORT_BREAK -> stringResource(Res.string.focus_session_phase_break)
        FocusPhase.LONG_BREAK -> stringResource(Res.string.focus_session_phase_long_break)
    }
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = CircleShape,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun FocusSegmentBar(
    completedSegments: Int,
    totalSegments: Int,
    overallProgress: Float,
) {
    val clampedProgress = overallProgress.coerceIn(0f, 1f)
    val safeTotal = totalSegments.coerceAtLeast(1)
    val currentSegmentIndex = (clampedProgress * safeTotal).toInt().coerceIn(0, safeTotal - 1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { clampedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val clampedCompleted = completedSegments.coerceIn(0, totalSegments)
            repeat(totalSegments.coerceAtLeast(0)) { index ->
                val color = when {
                    index < clampedCompleted -> MaterialTheme.colorScheme.secondary
                    index == currentSegmentIndex -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
    }
}

@Composable
private fun FocusQuoteBlock(state: FocusPomodoroUiState.Active) {
    val quoteDescription = stringResource(
        Res.string.focus_session_quote_content_description,
        state.quote.text,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = quoteDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "\"${state.quote.text}\"",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        val attribution = listOfNotNull(state.quote.character, state.quote.sourceTitle, state.quote.metadata)
            .joinToString(separator = " — ")
            .takeIf { it.isNotBlank() }
        if (attribution != null) {
            Text(
                text = attribution,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun FocusControls(
    state: FocusPomodoroUiState.Active,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val pauseOrResumeIcon = if (state.timerStatus == FocusTimerStatus.RUNNING) {
            Icons.Rounded.Pause
        } else {
            Icons.Rounded.PlayArrow
        }
        val pauseOrResumeDescription = if (state.timerStatus == FocusTimerStatus.RUNNING) {
            stringResource(Res.string.focus_session_pause_content_description)
        } else {
            stringResource(Res.string.focus_session_resume_content_description)
        }
        FocusCircularButton(
            onClick = onTogglePause,
            icon = { Icon(imageVector = pauseOrResumeIcon, contentDescription = null) },
            buttonDescription = pauseOrResumeDescription,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )

        FocusCircularButton(
            onClick = onEnd,
            icon = { Icon(imageVector = Icons.Rounded.Close, contentDescription = null) },
            buttonDescription = stringResource(Res.string.focus_session_end_content_description),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
private fun FocusCircularButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    buttonDescription: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .semantics { this.contentDescription = buttonDescription },
        shape = CircleShape,
        color = containerColor,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
    }
}

@Composable
private fun FocusConfirmDialog(
    onConfirmFinish: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.focus_session_confirm_end_title),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.focus_session_confirm_end_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmFinish) {
                Text(
                    text = stringResource(Res.string.focus_session_confirm_finish),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.focus_session_confirm_continue),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun FocusTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}

@Composable
private fun TopArcBackground(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawRect(color)
        val radius = size.width
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(x = size.width / 2f, y = size.height),
        )
    }
}

@Composable
private fun FocusLoadingState(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "...",
            style = MaterialTheme.typography.headlineMedium,
        )
        FocusTopBar(onNavigateBack = onNavigateBack)
    }
}

@Composable
private fun FocusCompletedState(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Focus complete!",
            style = MaterialTheme.typography.headlineMedium,
        )
        FocusTopBar(onNavigateBack = onNavigateBack)
    }
}

@Composable
private fun FocusErrorState(onNavigateBack: () -> Unit, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
        FocusTopBar(onNavigateBack = onNavigateBack)
    }
}
