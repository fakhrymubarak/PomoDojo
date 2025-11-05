package com.fakhry.pomodojo.preferences.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_break_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_enable_long_break
import com.fakhry.pomodojo.generated.resources.preferences_focus_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_long_break_after_title
import com.fakhry.pomodojo.generated.resources.preferences_long_break_timer_title
import com.fakhry.pomodojo.generated.resources.preferences_repeat_title
import com.fakhry.pomodojo.generated.resources.preferences_title_pomodoro_config
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PomodoroConfigSection(
    state: PreferencesUiModel,
    onRepeatCountChanged: (Int) -> Unit = {},
    onFocusSelected: (Int) -> Unit = {},
    onBreakSelected: (Int) -> Unit = {},
    onToggleLongBreak: (Boolean) -> Unit = {},
    onLongBreakAfterSelected: (Int) -> Unit = {},
    onLongBreakMinutesSelected: (Int) -> Unit = {},
) = this.run {
    val visibilityLongBreakSection = remember { MutableTransitionState(state.isLongBreakEnabled) }

    Text(
        text = stringResource(Res.string.preferences_title_pomodoro_config),
        style = MaterialTheme.typography.headlineMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
    )

    Spacer(modifier = Modifier.height(16.dp))

    RepeatSection(
        repeatCount = state.repeatCount,
        range = state.repeatRange,
        onRepeatCountChanged = onRepeatCountChanged,
    )

    Spacer(modifier = Modifier.height(16.dp))

    PreferenceOptionsCompose(
        title = stringResource(Res.string.preferences_focus_timer_title),
        options = state.focusOptions,
        onOptionSelected = {
            onFocusSelected(it)
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

    PreferenceOptionsCompose(
        title = stringResource(Res.string.preferences_break_timer_title),
        options = state.breakOptions,
        onOptionSelected = {
            onBreakSelected(it)
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

    LongBreakToggle(
        enabled = state.isLongBreakEnabled,
        onToggle = {
            onToggleLongBreak(it)
            visibilityLongBreakSection.targetState = it
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

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
            PreferenceOptionsCompose(
                title = stringResource(Res.string.preferences_long_break_after_title),
                options = state.longBreakAfterOptions,
                onOptionSelected = onLongBreakAfterSelected,
            )
            Spacer(modifier = Modifier.height(16.dp))

            PreferenceOptionsCompose(
                title = stringResource(Res.string.preferences_long_break_timer_title),
                options = state.longBreakOptions,
                onOptionSelected = onLongBreakMinutesSelected,
            )
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
private fun LongBreakToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = {
                    onToggle(it)

                    val hapticType =
                        if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                    hapticFeedback.performHapticFeedback(hapticType)
                },
            ).padding(horizontal = 20.dp, vertical = 12.dp),
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
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}