package com.fakhry.pomodojo.preferences.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_theme_title
import com.fakhry.pomodojo.generated.resources.preferences_title_pomodoro_appearance_config
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PreferenceAppearanceSection(
    state: PreferencesUiModel,
    onOptionSelected: (AppTheme) -> Unit,
) = this.run {

    Text(
        text = stringResource(Res.string.preferences_title_pomodoro_appearance_config),
        style = MaterialTheme.typography.headlineMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
    )
    Spacer(modifier = Modifier.height(16.dp))
    PreferenceOptionsCompose(
        title = stringResource(Res.string.preferences_theme_title),
        options = state.themeOptions,
        onOptionSelected = onOptionSelected,
    )
}