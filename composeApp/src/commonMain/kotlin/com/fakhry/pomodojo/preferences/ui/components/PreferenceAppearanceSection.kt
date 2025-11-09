package com.fakhry.pomodojo.preferences.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_theme_title
import com.fakhry.pomodojo.generated.resources.preferences_title_pomodoro_appearance_config
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.ui.RecompositionTags
import com.fakhry.pomodojo.preferences.ui.TrackRecomposition
import com.fakhry.pomodojo.preferences.ui.model.PreferenceOption
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.PreferenceAppearanceSection(
    themeOptions: ImmutableList<PreferenceOption<AppTheme>>,
    onOptionSelected: (AppTheme) -> Unit,
) = this.run {
    TrackRecomposition(RecompositionTags.APPEARANCE_SECTION)
    val colorScheme = MaterialTheme.colorScheme

    Text(
        text = stringResource(Res.string.preferences_title_pomodoro_appearance_config),
        style =
            MaterialTheme.typography.headlineMedium.copy(
                color = colorScheme.onBackground,
            ),
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        color = colorScheme.surface,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        PreferenceOptionsCompose(
            modifier = Modifier.padding(16.dp),
            title = stringResource(Res.string.preferences_theme_title),
            options = themeOptions,
            onOptionSelected = onOptionSelected,
        )
    }
}
