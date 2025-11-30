package com.fakhry.pomodojo.features.preferences.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.model.PreferenceOption
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> PreferenceOptionsCompose(
    modifier: Modifier = Modifier,
    title: String,
    options: ImmutableList<PreferenceOption<T>>,
    onOptionSelected: (T) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEach { option ->
                PreferenceOptionChip(
                    option = option,
                    onClick = {
                        onOptionSelected(option.value)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    },
                )
            }
        }
    }
}

@Composable
private fun <T> PreferenceOptionChip(option: PreferenceOption<T>, onClick: () -> Unit) {
    val isSelected = option.selected
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) {
        colorScheme.secondary
    } else {
        colorScheme.surfaceVariant
    }
    val borderColor = if (isSelected) {
        colorScheme.secondary
    } else {
        colorScheme.outline
    }
    val contentColor = if (isSelected) {
        colorScheme.onSecondary
    } else {
        colorScheme.onSurface
    }
    val alpha = if (option.enabled) 1f else 0.3f

    Surface(
        onClick = { if (option.enabled) onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        color = backgroundColor,
        modifier = Modifier.alpha(alpha),
    ) {
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = contentColor,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
