package com.fakhry.pomodojo.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_back_content_description
import com.fakhry.pomodojo.generated.resources.preferences_break_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_enable_long_break
import com.fakhry.pomodojo.generated.resources.preferences_focus_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_long_break_after_title
import com.fakhry.pomodojo.generated.resources.preferences_long_break_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_repeat_title
import com.fakhry.pomodojo.generated.resources.preferences_timeline_break_label
import com.fakhry.pomodojo.generated.resources.preferences_timeline_focus_label
import com.fakhry.pomodojo.generated.resources.preferences_timeline_preview_title
import com.fakhry.pomodojo.generated.resources.preferences_title
import com.fakhry.pomodojo.preferences.components.WheelNumbers
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.TimelinePreviewBuilder
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegment
import com.fakhry.pomodojo.preferences.ui.state.PreferenceOption
import com.fakhry.pomodojo.preferences.ui.state.PreferencesState
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.Primary
import com.fakhry.pomodojo.ui.theme.Secondary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Suppress("NonSkippableComposable")
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PreferencesViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PreferencesTopBar(onNavigateBack = onNavigateBack)
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                PreferencesContent(
                    state = state,
                    onRepeatCountChanged = viewModel::onRepeatCountChanged,
                    onFocusSelected = viewModel::onFocusOptionSelected,
                    onBreakSelected = viewModel::onBreakOptionSelected,
                    onToggleLongBreak = viewModel::onLongBreakEnabledToggled,
                    onLongBreakAfterSelected = viewModel::onLongBreakAfterSelected,
                    onLongBreakMinutesSelected = viewModel::onLongBreakMinutesSelected,
                )
            }
        }
    }
}

@Composable
private fun PreferencesTopBar(onNavigateBack: () -> Unit) {
    var isNavigatingBack by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(
            onClick = {
                if (!isNavigatingBack) {
                    onNavigateBack()
                    isNavigatingBack = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.preferences_back_content_description),
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
        Text(
            text = stringResource(Res.string.preferences_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
            ),
        )
    }
}

@Composable
private fun PreferencesContent(
    state: PreferencesState,
    onRepeatCountChanged: (Int) -> Unit = {},
    onFocusSelected: (Int) -> Unit = {},
    onBreakSelected: (Int) -> Unit = {},
    onToggleLongBreak: (Boolean) -> Unit = {},
    onLongBreakAfterSelected: (Int) -> Unit = {},
    onLongBreakMinutesSelected: (Int) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val visibilityLongBreakSection = remember { MutableTransitionState(state.isLongBreakEnabled) }
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        TimelinePreview(
            segments = state.timelineSegments,
        )

        RepeatSection(
            repeatCount = state.repeatCount,
            range = state.repeatRange,
            onRepeatCountChanged = onRepeatCountChanged,
        )

        PreferenceOptionsSection(
            title = stringResource(Res.string.preferences_focus_timer_title),
            options = state.focusOptions,
            onOptionSelected = {
                onFocusSelected(it)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
            },
        )

        PreferenceOptionsSection(
            title = stringResource(Res.string.preferences_break_timer_title),
            options = state.breakOptions,
            onOptionSelected = {
                onBreakSelected(it)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
            },
        )

        LongBreakToggle(
            enabled = state.isLongBreakEnabled,
            onToggle = {
                onToggleLongBreak(it)
                val hapticType =
                    if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                hapticFeedback.performHapticFeedback(hapticType)
                visibilityLongBreakSection.targetState = it
            },
        )

        AnimatedVisibility(
            visibleState = visibilityLongBreakSection,
            enter = expandVertically(
                animationSpec = tween()
            ),
            exit = shrinkVertically(
                animationSpec = tween()
            ),
        ) {
            Column {
                PreferenceOptionsSection(
                    title = stringResource(Res.string.preferences_long_break_after_title),
                    options = state.longBreakAfterOptions,
                    onOptionSelected = onLongBreakAfterSelected,
                )

                PreferenceOptionsSection(
                    title = stringResource(Res.string.preferences_long_break_timer_title),
                    options = state.longBreakOptions,
                    onOptionSelected = onLongBreakMinutesSelected,
                )
            }
        }
    }
}

@Composable
private fun RepeatSection(
    repeatCount: Int,
    range: IntRange,
    onRepeatCountChanged: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.preferences_repeat_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        WheelNumbers(
            start = range.first,
            end = range.last,
            selectedValue = repeatCount,
            onValueChange = onRepeatCountChanged,
        )
    }
}

@Composable
private fun PreferenceOptionsSection(
    title: String,
    options: ImmutableList<PreferenceOption<Int>>,
    onOptionSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEach { option ->
                PreferenceOptionChip(
                    option = option,
                    onClick = { onOptionSelected(option.value) },
                )
            }
        }
    }
}

@Composable
private fun PreferenceOptionChip(
    option: PreferenceOption<Int>,
    onClick: () -> Unit,
) {
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        color = backgroundColor,
        modifier = Modifier
            .alpha(alpha),
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

@Composable
private fun LongBreakToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = onToggle,
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.preferences_enable_long_break),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            Switch(
                checked = enabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
private fun TimelinePreview(segments: ImmutableList<TimelineSegment>) {
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
        val totalMinutes = segments.sumOf { it.durationMinutes }.coerceAtLeast(1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            segments.forEachIndexed { index, segment ->
                val weight = segment.durationMinutes / totalMinutes.toFloat()
                val color = when (segment) {
                    is TimelineSegment.Focus -> Secondary
                    is TimelineSegment.ShortBreak -> Primary
                    is TimelineSegment.LongBreak -> Primary.copy(alpha = 0.8f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weight)
                        .background(color),
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
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
    )
}

@Preview
@Composable
fun PreferencesContentPreview() {
    val previewPreferences = PomodoroPreferences(
        repeatCount = 4,
        focusMinutes = 25,
        breakMinutes = 5,
        longBreakEnabled = true,
        longBreakAfter = 4,
        longBreakMinutes = 10,
    )

    PomoDojoTheme {
        PreferencesContent(previewPreferencesState(previewPreferences))
    }
}

private fun previewPreferencesState(preferences: PomodoroPreferences): PreferencesState {
    val focusOptions = listOf(10, 25, 50).map { minutes ->
        PreferenceOption(
            label = "$minutes mins",
            value = minutes,
            selected = minutes == preferences.focusMinutes,
        )
    }.toPersistentList()
    val breakOptions = listOf(2, 5, 10).map { minutes ->
        PreferenceOption(
            label = "$minutes mins",
            value = minutes,
            selected = minutes == preferences.breakMinutes,
        )
    }.toPersistentList()
    val longBreakEnabled = preferences.longBreakEnabled
    val longBreakAfterOptions = listOf(6, 4, 2).map { count ->
        PreferenceOption(
            label = "$count focuses",
            value = count,
            selected = count == preferences.longBreakAfter,
            enabled = longBreakEnabled,
        )
    }.toPersistentList()
    val longBreakOptions = listOf(4, 10, 20).map { minutes ->
        PreferenceOption(
            label = "$minutes mins",
            value = minutes,
            selected = minutes == preferences.longBreakMinutes,
            enabled = longBreakEnabled,
        )
    }.toPersistentList()

    return PreferencesState(
        repeatCount = preferences.repeatCount,
        focusOptions = focusOptions,
        breakOptions = breakOptions,
        isLongBreakEnabled = longBreakEnabled,
        longBreakAfterOptions = longBreakAfterOptions,
        longBreakOptions = longBreakOptions,
        timelineSegments = TimelinePreviewBuilder().build(preferences),
        isLoading = false,
    )
}
