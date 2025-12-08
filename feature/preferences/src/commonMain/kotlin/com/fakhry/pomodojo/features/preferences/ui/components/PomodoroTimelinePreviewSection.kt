package com.fakhry.pomodojo.features.preferences.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.components.TimelineHoursSplit
import com.fakhry.pomodojo.core.designsystem.components.TimelineLegends
import com.fakhry.pomodojo.core.designsystem.components.TimelinePreview
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.preferences_timeline_preview_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.preferences_title_pomodoro_timeline_preview
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PomodoroTimelinePreviewSection(timeline: TimelineUiModel) = this.run {
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
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.preferences_timeline_preview_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            TimelinePreview(timeline.segments)
            Spacer(modifier = Modifier.height(4.dp))
            TimelineHoursSplit(timeline.hourSplits)
            Spacer(modifier = Modifier.height(12.dp))
            TimelineLegends()
        }
    }
}
