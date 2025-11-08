package com.fakhry.pomodojo.focus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_finish
import com.fakhry.pomodojo.generated.resources.focus_session_header_cycle_count
import com.fakhry.pomodojo.generated.resources.focus_session_header_title
import com.fakhry.pomodojo.generated.resources.focus_session_phase_break
import com.fakhry.pomodojo.generated.resources.focus_session_phase_focus
import com.fakhry.pomodojo.generated.resources.focus_session_phase_long_break
import com.fakhry.pomodojo.generated.resources.focus_session_quote_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_timeline_title
import com.fakhry.pomodojo.generated.resources.minutes
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import com.fakhry.pomodojo.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.ui.theme.LongBreakHighlight
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("NonSkippableComposable")
@Composable
fun PomodoroSessionScreen(
    onSessionCompleted: () -> Unit,
    viewModel: FocusPomodoroViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    BackHandler {
        viewModel.onEndClicked()
    }

    LaunchedEffect(isComplete) {
        if (isComplete) onSessionCompleted()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        println("Trace currentState $state")
        Column(modifier = Modifier.fillMaxSize()) {
            FocusSessionHeader(state = state)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
            }
        }
    }
}

//@Composable
//private fun FocusActiveState(
//    state: PomodoroSessionUiState,
//    timelineSegments: ImmutableList<TimelineSegmentUiModel>,
//    timelineProgress: ImmutableList<Float>,
//    hourSplits: ImmutableList<Int>,
//    onTogglePause: () -> Unit,
//    onEnd: () -> Unit,
//    onDismissConfirm: () -> Unit,
//    onConfirmFinish: () -> Unit,
//    onTick: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    LaunchedEffect(state.timerStatus, state.remainingSeconds) {
//        if (state.timerStatus == FocusTimerStatus.RUNNING) {
//            delay(1)
//            onTick()
//        }
//    }
//
//    val scrollState = rememberScrollState()
//
//    Box(modifier = modifier) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 24.dp, vertical = 24.dp),
//        ) {
//            Column(
//                modifier = Modifier
//                    .weight(1f, fill = true)
//                    .verticalScroll(scrollState),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(24.dp),
//            ) {
//                FocusTimerDisplay(
//                    formattedTime = state.formattedTime,
//                    progress = state.progress,
//                    status = state.timerStatus,
//                    phase = state.phase,
//                )
//
//                FocusTimelineProgress(
//                    segments = timelineSegments,
//                    segmentProgress = timelineProgress,
//                    hourSplits = hourSplits,
//                )
//
//                FocusQuoteBlock(state)
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            FocusControls(
//                state = state,
//                onTogglePause = onTogglePause,
//                onEnd = onEnd,
//            )
//        }
//
//        if (state.isShowConfirmEndDialog) {
//            FocusConfirmDialog(
//                onConfirmFinish = onConfirmFinish,
//                onDismiss = onDismissConfirm,
//            )
//        }
//    }
//}

//@Composable
//private fun FocusTimerDisplay(
//    formattedTime: String,
//    progress: Float,
//    status: FocusTimerStatus,
//    phase: PomodoroPhase,
//) {
//    val animatedProgress by animateFloatAsState(
//        targetValue = progress.coerceIn(0f, 1f),
//        label = "focus-timer-progress",
//    )
//    val colorScheme = MaterialTheme.colorScheme
//
//    Box(
//        modifier = Modifier.size(280.dp),
//        contentAlignment = Alignment.Center,
//    ) {
//        PomodoroTimerDecoration()
//
//        Canvas(
//            modifier = Modifier
//                .matchParentSize()
//                .padding(24.dp),
//        ) {
//            val strokeWidth = 18.dp.toPx()
//            val radius = (size.minDimension / 2f) - strokeWidth / 2f
//            drawCircle(
//                color = colorScheme.surfaceVariant,
//                radius = radius,
//                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
//                center = center,
//            )
//            drawArc(
//                color = colorScheme.primary,
//                startAngle = -90f,
//                sweepAngle = 360 * animatedProgress,
//                useCenter = false,
//                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
//                topLeft = center - Offset(radius, radius),
//                size = Size(radius * 2, radius * 2),
//            )
//        }
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            Text(
//                text = formattedTime,
//                style = MaterialTheme.typography.displayLarge.copy(
//                    color = colorScheme.onBackground,
//                ),
//            )
//            FocusPhaseChip(phase = phase)
//        }
//
//        AnimatedVisibility(
//            visible = status == FocusTimerStatus.PAUSED,
//            enter = fadeIn(),
//            exit = fadeOut(),
//            modifier = Modifier.align(Alignment.Center)
//        ) {
//            Surface(
//                color = colorScheme.onPrimary.copy(alpha = 0.85f),
//                shape = CircleShape,
//                tonalElevation = 4.dp,
//            ) {
//                Text(
//                    text = stringResource(Res.string.focus_session_paused_label),
//                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
//                    style = MaterialTheme.typography.titleMedium.copy(
//                        color = colorScheme.primary,
//                        fontWeight = FontWeight.SemiBold,
//                    ),
//                )
//            }
//        }
//    }
//}

@Composable
private fun FocusPhaseChip(phase: PhaseType) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = CircleShape,
    ) {
        Text(
            text = focusPhaseLabel(phase),
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun FocusTimelineProgress(
    segments: ImmutableList<TimelineSegmentUiModel>,
    segmentProgress: ImmutableList<Float>,
    hourSplits: ImmutableList<Int>,
) {
    if (segments.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.focus_session_timeline_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TimelineProgressRow(
                    segments = segments,
                    segmentProgress = segmentProgress,
                )
                TimelineHoursSplit(hourSplits = hourSplits)
                TimelineLegend()
            }
        }
    }
}

@Composable
private fun TimelineProgressRow(
    segments: ImmutableList<TimelineSegmentUiModel>,
    segmentProgress: ImmutableList<Float>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        segments.forEachIndexed { index, segment ->
            val progress = segmentProgress.getOrNull(index)?.coerceIn(0f, 1f) ?: 0f
            val segmentColor = timelineSegmentColor(segment)

            Box(
                modifier = Modifier
                    .weight(segment.duration.coerceAtLeast(1).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(segmentColor),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHoursSplit(hourSplits: ImmutableList<Int>) {
    if (hourSplits.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        hourSplits.forEach { duration ->
            Column(
                modifier = Modifier.weight(duration.coerceAtLeast(1).toFloat()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1.dp)),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.matchParentSize(),
                        content = {},
                    )
                }
                Text(
                    text = pluralStringResource(Res.plurals.minutes, duration, duration),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TimelineLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = Secondary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.focus_session_phase_focus),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = Primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.focus_session_phase_break),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = LongBreakHighlight)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.focus_session_phase_long_break),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape),
    ) {
        Surface(
            color = color,
            modifier = Modifier.matchParentSize(),
            shape = CircleShape,
            content = {},
        )
    }
}

private fun timelineSegmentColor(segment: TimelineSegmentUiModel): Color = when (segment) {
    is TimelineSegmentUiModel.Focus -> Secondary
    is TimelineSegmentUiModel.ShortBreak -> Primary
    is TimelineSegmentUiModel.LongBreak -> LongBreakHighlight
}

@Composable
private fun FocusQuoteBlock(state: PomodoroSessionUiState) {
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
        val attribution =
            listOfNotNull(state.quote.character, state.quote.sourceTitle, state.quote.metadata)
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

//@Composable
//private fun FocusControls(
//    state: PomodoroSessionUiState,
//    onTogglePause: () -> Unit,
//    onEnd: () -> Unit,
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 8.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly,
//    ) {
//        val pauseOrResumeIcon = if (state.timerStatus == FocusTimerStatus.RUNNING) {
//            Icons.Rounded.Pause
//        } else {
//            Icons.Rounded.PlayArrow
//        }
//        val pauseOrResumeDescription = if (state.timerStatus == FocusTimerStatus.RUNNING) {
//            stringResource(Res.string.focus_session_pause_content_description)
//        } else {
//            stringResource(Res.string.focus_session_resume_content_description)
//        }
//        FocusCircularButton(
//            onClick = onTogglePause,
//            icon = { Icon(imageVector = pauseOrResumeIcon, contentDescription = null) },
//            buttonDescription = pauseOrResumeDescription,
//            containerColor = MaterialTheme.colorScheme.primary,
//            contentColor = MaterialTheme.colorScheme.onPrimary,
//        )
//
//        FocusCircularButton(
//            onClick = onEnd,
//            icon = { Icon(imageVector = Icons.Rounded.Close, contentDescription = null) },
//            buttonDescription = stringResource(Res.string.focus_session_end_content_description),
//            containerColor = MaterialTheme.colorScheme.error,
//            contentColor = MaterialTheme.colorScheme.onError,
//        )
//    }
//}

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
private fun FocusSessionHeader(state: PomodoroSessionUiState) {
    val activePhase = state.activePhase
    BgHeaderCanvas {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.focus_session_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            val subtitle = state.let {
                val phaseLabel = focusPhaseLabel(activePhase.type)
                val cycleLabel = stringResource(
                    Res.string.focus_session_header_cycle_count,
                    activePhase.cycleNumber,
                    it.totalCycle,
                )
                "$phaseLabel • $cycleLabel"
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

@Composable
private fun FocusCompletedState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Focus complete!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
    }
}

@Composable
private fun FocusErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun focusPhaseLabel(phase: PhaseType): String = when (phase) {
    PhaseType.FOCUS -> stringResource(Res.string.focus_session_phase_focus)
    PhaseType.SHORT_BREAK -> stringResource(Res.string.focus_session_phase_break)
    PhaseType.LONG_BREAK -> stringResource(Res.string.focus_session_phase_long_break)
}