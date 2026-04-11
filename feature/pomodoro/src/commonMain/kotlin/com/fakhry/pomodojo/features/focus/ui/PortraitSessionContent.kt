package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.components.PomodoroTimerSection
import com.fakhry.pomodojo.core.designsystem.components.TimelineHoursSplit
import com.fakhry.pomodojo.core.designsystem.components.TimelineLegends
import com.fakhry.pomodojo.core.designsystem.components.TimelinePreview
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_quote_content_description
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_timeline_title
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.features.focus.ui.components.PomodoroSessionHeaderSection
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PortraitSessionContent(
    state: PomodoroSessionUiState,
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
) {
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
            isTimerRunning = isTimerRunning,
            onTogglePause = onTogglePause,
            onEnd = onEnd,
        )
    }
}

@Composable
internal fun ColumnScope.PomodoroTimelineSessionSection(modifier: Modifier, timeline: TimelineUiModel) =
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
