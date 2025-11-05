package com.fakhry.pomodojo.preferences.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_timeline_break_label
import com.fakhry.pomodojo.generated.resources.preferences_timeline_focus_label
import com.fakhry.pomodojo.generated.resources.preferences_timeline_long_break_label
import com.fakhry.pomodojo.generated.resources.preferences_timeline_preview_title
import com.fakhry.pomodojo.generated.resources.preferences_title_pomodoro_timeline_preview
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import com.fakhry.pomodojo.ui.theme.LongBreakHighlight
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PomodoroTimelinePreviewSection(segments: ImmutableList<TimelineSegmentUiModel>) =
    this.run {
        val colorScheme = MaterialTheme.colorScheme

        Text(
            text = stringResource(Res.string.preferences_title_pomodoro_timeline_preview),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = colorScheme.onBackground,
            ),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colorScheme.outline),
            color = colorScheme.surface,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                TimelinePreview(segments)
            }
        }
    }

@Composable
private fun TimelinePreview(segments: ImmutableList<TimelineSegmentUiModel>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Res.string.preferences_timeline_preview_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)),
        ) {
            segments.forEachIndexed { index, segment ->
                val weight = segment.weight
                val color = when (segment) {
                    is TimelineSegmentUiModel.Focus -> Secondary
                    is TimelineSegmentUiModel.ShortBreak -> Primary
                    is TimelineSegmentUiModel.LongBreak -> LongBreakHighlight
                }
                Box(
                    modifier = Modifier.fillMaxHeight().weight(weight).background(color),
                )
                if (index != segments.lastIndex) {
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = Secondary)
            Text(
                text = stringResource(Res.string.preferences_timeline_focus_label),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            LegendDot(color = Primary)
            Text(
                text = stringResource(Res.string.preferences_timeline_break_label),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            LegendDot(color = LongBreakHighlight)
            Text(
                text = stringResource(Res.string.preferences_timeline_long_break_label),
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
        modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color),
    )
}