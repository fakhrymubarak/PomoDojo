package com.fakhry.pomodojo.focus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.dashboard.ui.components.PomodoroTimerSection
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.ui.components.PomodoroSessionHeaderSection
import com.fakhry.pomodojo.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.focus.ui.viewmodel.PomodoroSessionViewModel
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.generated.resources.focus_session_confirm_finish
import com.fakhry.pomodojo.generated.resources.focus_session_end_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_pause_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_phase_break
import com.fakhry.pomodojo.generated.resources.focus_session_phase_focus
import com.fakhry.pomodojo.generated.resources.focus_session_phase_long_break
import com.fakhry.pomodojo.generated.resources.focus_session_quote_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_resume_content_description
import com.fakhry.pomodojo.generated.resources.focus_session_timeline_title
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.ui.components.TimelineHoursSplit
import com.fakhry.pomodojo.preferences.ui.components.TimelineLegends
import com.fakhry.pomodojo.preferences.ui.components.TimelinePreview
import com.fakhry.pomodojo.preferences.ui.mapper.mapToTimelineSegmentsUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val state = viewModel.collectAsState().value
            val activeSegment = state.activeSegment

            PomodoroSessionHeaderSection(
                timerType = activeSegment.type,
                cycleNumber = activeSegment.cycleNumber,
                totalCycle = state.totalCycle,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PomodoroTimerSection(
                segmentType = activeSegment.type,
                formattedTime = activeSegment.timer.formattedTime,
                progress = activeSegment.timer.progress,
            )
            Spacer(modifier = Modifier.height(32.dp))
            FocusQuoteBlock(modifier = Modifier.padding(horizontal = 16.dp), quote = state.quote)
            Spacer(modifier = Modifier.height(32.dp))
            PomodoroTimelineSessionSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                timeline = state.timeline,
            )
            Spacer(modifier = Modifier.height(32.dp))
            FocusControls(
                isTimerRunning = activeSegment.timerStatus == TimerStatusDomain.RUNNING,
                onTogglePause = viewModel::togglePauseResume,
                onEnd = viewModel::onEndClicked,
            )
        }
    }

    if (showEndDialog) {
        FocusConfirmDialog(
            onConfirmFinish = viewModel::onConfirmFinish,
            onDismiss = viewModel::onDismissConfirmEnd,
        )
    }
}

@Composable
fun ColumnScope.PomodoroTimelineSessionSection(modifier: Modifier, timeline: TimelineUiModel) =
    this.run {
        val colorScheme = MaterialTheme.colorScheme

        Text(
            text = stringResource(Res.string.focus_session_timeline_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            ),
            modifier = modifier.fillMaxWidth(),
        )

        Surface(
            modifier = modifier.padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colorScheme.outline),
            color = colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimelinePreview(timeline.segments)
                Spacer(modifier = Modifier.height(4.dp))
                TimelineHoursSplit(timeline.hourSplits)
                Spacer(modifier = Modifier.height(12.dp))
                TimelineLegends()
            }
        }
    }

@Composable
private fun FocusQuoteBlock(modifier: Modifier, quote: QuoteContent) {
    val quoteDescription = stringResource(
        Res.string.focus_session_quote_content_description,
        quote.text,
    )
    Column(
        modifier = modifier.fillMaxWidth().semantics { contentDescription = quoteDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "\"${quote.text}\"",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        val attribution = quote.attribution()
        if (attribution.isNotBlank()) {
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
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit = {},
    onEnd: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val icon = if (isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
        val description = if (isTimerRunning) {
            stringResource(Res.string.focus_session_pause_content_description)
        } else {
            stringResource(Res.string.focus_session_resume_content_description)
        }
        FocusCircularButton(
            onClick = onTogglePause,
            icon = { Icon(imageVector = icon, contentDescription = null) },
            buttonDescription = description,
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
        modifier = Modifier.size(64.dp).semantics { this.contentDescription = buttonDescription },
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
private fun FocusConfirmDialog(onConfirmFinish: () -> Unit, onDismiss: () -> Unit) {
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
fun focusPhaseLabel(phase: TimerType) = when (phase) {
    TimerType.FOCUS -> stringResource(Res.string.focus_session_phase_focus)
    TimerType.SHORT_BREAK -> stringResource(Res.string.focus_session_phase_break)
    TimerType.LONG_BREAK -> stringResource(Res.string.focus_session_phase_long_break)
}

@Preview
@Composable
private fun PomodoroSessionContentPreview() {
    val preferences = PreferencesDomain()
    val timerSegments = BuildTimerSegmentsUseCase().invoke(
        0L,
        preferences,
    ).mapToTimelineSegmentsUi(1_000L)
    val state = PomodoroSessionUiState(
        totalCycle = 4,
        activeSegment = timerSegments.first(),
        timeline = TimelineUiModel(
            segments = timerSegments,
            hourSplits = BuildHourSplitTimelineUseCase().invoke(preferences).toPersistentList(),
        ),
    )

    PomoDojoTheme {
        val activeSegment = state.activeSegment
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PomodoroSessionHeaderSection(
                timerType = activeSegment.type,
                cycleNumber = activeSegment.cycleNumber,
                totalCycle = state.totalCycle,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PomodoroTimerSection(
                segmentType = activeSegment.type,
                formattedTime = activeSegment.timer.formattedTime,
                progress = activeSegment.timer.progress,
            )
            Spacer(modifier = Modifier.height(32.dp))
            FocusQuoteBlock(modifier = Modifier.padding(horizontal = 16.dp), quote = state.quote)
            Spacer(modifier = Modifier.height(32.dp))
            PomodoroTimelineSessionSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                timeline = state.timeline,
            )
            Spacer(modifier = Modifier.height(32.dp))
            FocusControls(
                isTimerRunning = activeSegment.timerStatus == TimerStatusDomain.RUNNING,
            )
        }
    }
}
