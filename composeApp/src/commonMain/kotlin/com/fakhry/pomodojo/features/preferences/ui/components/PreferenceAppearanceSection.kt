package com.fakhry.pomodojo.features.preferences.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.ui.model.PreferenceOption
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_always_on_display_title
import com.fakhry.pomodojo.generated.resources.preferences_theme_title
import com.fakhry.pomodojo.generated.resources.preferences_title_pomodoro_appearance_config
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PreferenceAppearanceSection(
    themeOptions: ImmutableList<PreferenceOption<AppTheme>>,
    onOptionSelected: (AppTheme) -> Unit,
    isAlwaysOnDisplayEnabled: Boolean,
    onAlwaysOnDisplayToggled: (Boolean) -> Unit,
) = this.run {
    val colorScheme = MaterialTheme.colorScheme

    Text(
        text = stringResource(Res.string.preferences_title_pomodoro_appearance_config),
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
        Column(modifier = Modifier.padding(16.dp)) {
            PreferenceOptionsCompose(
                title = stringResource(Res.string.preferences_theme_title),
                options = themeOptions,
                onOptionSelected = onOptionSelected,
            )

            Spacer(modifier = Modifier.height(24.dp))

            PreferenceToggleRow(
                label = stringResource(Res.string.preferences_always_on_display_title),
                checked = isAlwaysOnDisplayEnabled,
                onToggle = onAlwaysOnDisplayToggled,
            )
        }
    }
}

@Composable
private fun PreferenceToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth().toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = {
                onToggle(it)
                val hapticType =
                    if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                hapticFeedback.performHapticFeedback(hapticType)
            },
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        Switch(
            checked = checked,
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
