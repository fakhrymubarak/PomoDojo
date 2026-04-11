package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.core.designsystem.components.TimelinePreview
import com.fakhry.pomodojo.core.designsystem.components.focusPhaseLabel
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_end_content_description
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_pause_content_description
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_resume_content_description
import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.theme.LongBreakHighlight
import com.fakhry.pomodojo.core.designsystem.theme.Primary
import com.fakhry.pomodojo.core.designsystem.theme.Secondary
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LandscapeSessionContent(
    state: PomodoroSessionUiState,
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
) {
    val activeSegment = state.activeSegment
    Column(modifier = Modifier.fillMaxSize()) {
        // Main row: controls toggle on the left, large timer text in the center
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LandscapeControlsToggle(
                isTimerRunning = isTimerRunning,
                onTogglePause = onTogglePause,
                onEnd = onEnd,
            )
            LandscapeTimerText(
                modifier = Modifier.weight(1f),
                segmentType = activeSegment.type,
                formattedTime = activeSegment.timer.formattedTime,
            )
        }

        // Bottom: compact timeline bar (bar only, no title/legends)
        CompactTimelineBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            segments = state.timeline.segments,
        )
    }
}

@Composable
private fun LandscapeTimerText(
    modifier: Modifier = Modifier,
    segmentType: TimerTypeUi,
    formattedTime: String,
) {
    val color = when (segmentType) {
        TimerTypeUi.FOCUS -> Secondary
        TimerTypeUi.SHORT_BREAK -> Primary
        TimerTypeUi.LONG_BREAK -> LongBreakHighlight
    }
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(0.75f),
        contentAlignment = Alignment.Center,
    ) {
        // Font size scales with 22% of the 75%-width container
        val fontSize = (maxWidth.value * 0.22f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            LandscapePhaseChip(phase = segmentType, color = color)
        }
    }
}

@Composable
private fun LandscapePhaseChip(phase: TimerTypeUi, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
    ) {
        Text(
            text = focusPhaseLabel(phase),
            style = MaterialTheme.typography.labelLarge.copy(
                color = color,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun LandscapeControlsToggle(
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
) {
    var showControls by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Controls slide in from the left when revealed
        AnimatedVisibility(
            visible = showControls,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val pauseIcon = if (isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
                val pauseDescription = if (isTimerRunning) {
                    stringResource(Res.string.focus_session_pause_content_description)
                } else {
                    stringResource(Res.string.focus_session_resume_content_description)
                }
                FocusCircularButton(
                    onClick = onTogglePause,
                    icon = { Icon(imageVector = pauseIcon, contentDescription = null) },
                    buttonDescription = pauseDescription,
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

        // Arrow toggle — rotates 180° when controls are visible
        val arrowRotation by animateFloatAsState(
            targetValue = if (showControls) 180f else 0f,
            label = "landscape-controls-arrow-rotation",
        )
        IconButton(onClick = { showControls = !showControls }) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = if (showControls) "Hide controls" else "Show controls",
                modifier = Modifier.rotate(arrowRotation),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun CompactTimelineBar(
    modifier: Modifier = Modifier,
    segments: ImmutableList<TimelineSegmentUi>,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimelinePreview(segments)
        }
    }
}
