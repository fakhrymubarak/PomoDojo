package com.fakhry.pomodojo.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.fakhry.pomodojo.generated.resources.focus_session_header_cycle_count
import com.fakhry.pomodojo.generated.resources.focus_session_header_preparing
import com.fakhry.pomodojo.generated.resources.focus_session_header_title
import com.fakhry.pomodojo.generated.resources.focus_session_pause_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_paused_label
import com.fakhry.pomodojo.generated.resources.focus_session_phase_break
import com.fakhry.pomodojo.generated.resources.focus_session_phase_focus
import com.fakhry.pomodojo.generated.resources.focus_session_phase_long_break
import com.fakhry.pomodojo.generated.resources.focus_session_quote_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_resume_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_timeline_title
import com.fakhry.pomodojo.generated.resources.minutes
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.ui.mapper.mapToTimelineSegmentsUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import com.fakhry.pomodojo.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.ui.components.PomodoroTimerDecoration
import com.fakhry.pomodojo.ui.theme.LongBreakHighlight
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("NonSkippableComposable")
@Composable
fun FocusPomodoroScreen(
    onSessionCompleted: () -> Unit,
    viewModel: FocusPomodoroViewModel = koinViewModel(),
) {
    val preferencesRepository: PreferencesRepository = koinInject()
    val preferences by preferencesRepository.preferences.collectAsState(initial = PreferencesDomain())
    val state by viewModel.state.collectAsState()
    val activeState = state as? FocusPomodoroUiState.Active

    val focusTimelineBuilder = remember { BuildFocusTimelineUseCase() }
    val hourSplitBuilder = remember { BuildHourSplitTimelineUseCase() }

    val timelineSegments = remember(preferences) {
        focusTimelineBuilder(preferences).mapToTimelineSegmentsUi()
    }
    val timelineHourSplits = remember(preferences) {
        hourSplitBuilder(preferences).toPersistentList()
    }
    val timelineProgress = remember(timelineSegments, activeState) {
        computeTimelineProgress(timelineSegments, activeState)
    }

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

    BackHandler {
        viewModel.onEndClicked()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FocusSessionHeader(activeState = activeState)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (val uiState = state) {
                    FocusPomodoroUiState.Loading,
                    FocusPomodoroUiState.Completing -> FocusLoadingState(Modifier.fillMaxSize())
                    is FocusPomodoroUiState.Error -> FocusErrorState(
                        message = uiState.message,
                        modifier = Modifier.fillMaxSize(),
                    )
                    is FocusPomodoroUiState.Completed -> FocusCompletedState(Modifier.fillMaxSize())
                    is FocusPomodoroUiState.Active -> FocusActiveState(
                        state = uiState,
                        timelineSegments = timelineSegments,
                        timelineProgress = timelineProgress,
                        hourSplits = timelineHourSplits,
                        onTogglePause = viewModel::togglePauseResume,
                        onEnd = viewModel::onEndClicked,
                        onDismissConfirm = viewModel::onDismissConfirmEnd,
                        onConfirmFinish = viewModel::onConfirmFinish,
                        onTick = viewModel::decrementTimer,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusActiveState(
    state: FocusPomodoroUiState.Active,
    timelineSegments: ImmutableList<TimelineSegmentUiModel>,
    timelineProgress: ImmutableList<Float>,
    hourSplits: ImmutableList<Int>,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
    onDismissConfirm: () -> Unit,
    onConfirmFinish: () -> Unit,
    onTick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.timerStatus, state.remainingSeconds) {
        if (state.timerStatus == FocusTimerStatus.RUNNING) {
            delay(1)
            onTick()
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                FocusTimerDisplay(
                    formattedTime = state.formattedTime,
                    progress = state.progress,
                    status = state.timerStatus,
                    phase = state.phase,
                )

                FocusTimelineProgress(
                    segments = timelineSegments,
                    segmentProgress = timelineProgress,
                    hourSplits = hourSplits,
                )

                FocusQuoteBlock(state)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "focus-timer-progress",
    )
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        PomodoroTimerDecoration()

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(24.dp),
        ) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension / 2f) - strokeWidth / 2f
            drawCircle(
                color = colorScheme.surfaceVariant,
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                center = center,
            )
            drawArc(
                color = colorScheme.primary,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = center - Offset(radius, radius),
                size = Size(radius * 2, radius * 2),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = colorScheme.onBackground,
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
                color = colorScheme.onPrimary.copy(alpha = 0.85f),
                shape = CircleShape,
                tonalElevation = 4.dp,
            ) {
                Text(
                    text = stringResource(Res.string.focus_session_paused_label),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FocusPhaseChip(phase: FocusPhase) {
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
            .padding(bottom = 8.dp),
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
private fun FocusSessionHeader(activeState: FocusPomodoroUiState.Active?) {
    BgHeaderCanvas {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.focus_session_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            val subtitle = activeState?.let {
                val phaseLabel = focusPhaseLabel(it.phase)
                val cycleLabel = stringResource(
                    Res.string.focus_session_header_cycle_count,
                    currentCycle(it),
                    it.totalSegments,
                )
                "$phaseLabel • $cycleLabel"
            } ?: stringResource(Res.string.focus_session_header_preparing)
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
private fun FocusLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
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
private fun focusPhaseLabel(phase: FocusPhase): String = when (phase) {
    FocusPhase.FOCUS -> stringResource(Res.string.focus_session_phase_focus)
    FocusPhase.SHORT_BREAK -> stringResource(Res.string.focus_session_phase_break)
    FocusPhase.LONG_BREAK -> stringResource(Res.string.focus_session_phase_long_break)
}

private fun computeTimelineProgress(
    segments: ImmutableList<TimelineSegmentUiModel>,
    activeState: FocusPomodoroUiState.Active?,
): ImmutableList<Float> {
    if (segments.isEmpty()) return persistentListOf()
    if (activeState == null) {
        return segments.map { 0f }.toPersistentList()
    }

    val completedFocus = activeState.completedSegments.coerceAtLeast(0)
    val currentPhase = activeState.phase
    val currentProgress = activeState.progress.coerceIn(0f, 1f)

    var focusSeen = 0
    val result = segments.map { segment ->
        when (segment) {
            is TimelineSegmentUiModel.Focus -> {
                focusSeen += 1
                when {
                    currentPhase == FocusPhase.FOCUS && focusSeen == completedFocus + 1 -> currentProgress
                    focusSeen <= completedFocus -> 1f
                    else -> 0f
                }
            }

            is TimelineSegmentUiModel.ShortBreak -> when {
                currentPhase == FocusPhase.SHORT_BREAK && focusSeen == completedFocus -> currentProgress
                focusSeen < completedFocus -> 1f
                else -> 0f
            }

            is TimelineSegmentUiModel.LongBreak -> when {
                currentPhase == FocusPhase.LONG_BREAK && focusSeen == completedFocus -> currentProgress
                focusSeen < completedFocus -> 1f
                else -> 0f
            }
        }
    }
    return result.toPersistentList()
}

private fun currentCycle(state: FocusPomodoroUiState.Active): Int {
    val base = when (state.phase) {
        FocusPhase.FOCUS -> state.completedSegments + 1
        FocusPhase.SHORT_BREAK,
        FocusPhase.LONG_BREAK -> state.completedSegments
    }
    return base.coerceIn(1, state.totalSegments)
}
