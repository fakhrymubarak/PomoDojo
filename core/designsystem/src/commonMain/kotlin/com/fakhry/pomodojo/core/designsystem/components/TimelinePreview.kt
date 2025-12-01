package com.fakhry.pomodojo.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.minutes
import com.fakhry.pomodojo.core.designsystem.generated.resources.preferences_timeline_break_label
import com.fakhry.pomodojo.core.designsystem.generated.resources.preferences_timeline_focus_label
import com.fakhry.pomodojo.core.designsystem.generated.resources.preferences_timeline_long_break_label
import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.theme.LongBreakHighlight
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import com.fakhry.pomodojo.core.designsystem.theme.Primary
import com.fakhry.pomodojo.core.designsystem.theme.Secondary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun ColumnScope.TimelinePreview(segments: ImmutableList<TimelineSegmentUi>) = this.run {
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)),
    ) {
        segments.forEachIndexed { index, segment ->
            val progress = segment.timer.progress.coerceIn(0f, 1f)
            val color = when (segment.type) {
                TimerTypeUi.FOCUS -> Secondary
                TimerTypeUi.SHORT_BREAK -> Primary
                TimerTypeUi.LONG_BREAK -> LongBreakHighlight
            }
            Box(
                modifier = Modifier.fillMaxHeight().weight(segment.timer.durationEpochMs.toFloat())
                    .clip(
                        RoundedCornerShape(2.dp),
                    ).background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(
                            progress,
                        ).background(color),
                    )
                }
            }
            if (index != segments.lastIndex) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
fun TimelineHoursSplit(hourSplits: ImmutableList<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        hourSplits.forEachIndexed { index, duration ->
            Column(
                modifier = Modifier.weight(duration.toFloat()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier.height(
                        1.dp,
                    ).fillMaxWidth().background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = pluralStringResource(Res.plurals.minutes, duration, duration),
                    minLines = 2,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

            if (index != hourSplits.lastIndex) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun ColumnScope.TimelineLegends() = this.run {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(color = Secondary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(Res.string.preferences_timeline_focus_label),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        Spacer(modifier = Modifier.width(16.dp))

        LegendDot(color = Primary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(Res.string.preferences_timeline_break_label),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        Spacer(modifier = Modifier.width(16.dp))

        LegendDot(color = LongBreakHighlight)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(Res.string.preferences_timeline_long_break_label),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color),
    )
}

@Preview
@Composable
private fun PomodoroTimelinePreviewSectionPreview() {
    val timeline = TimelineUiModel(
        segments = persistentListOf(
            TimelineSegmentUi(timerStatus = TimerStatusUi.COMPLETED),
            TimelineSegmentUi(
                type = TimerTypeUi.SHORT_BREAK,
                timerStatus = TimerStatusUi.COMPLETED,
            ),
            TimelineSegmentUi(timerStatus = TimerStatusUi.RUNNING),
            TimelineSegmentUi(
                type = TimerTypeUi.SHORT_BREAK,
            ),
            TimelineSegmentUi(),
            TimelineSegmentUi(
                type = TimerTypeUi.LONG_BREAK,
            ),
            TimelineSegmentUi(),
        ),
        hourSplits = persistentListOf(60, 55),
    )
    PomoDojoTheme {
        Column {
            TimelinePreview(timeline.segments)
        }
    }
}
