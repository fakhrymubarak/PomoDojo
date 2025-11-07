package com.fakhry.pomodojo.preferences.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.preferences_back_content_description
import com.fakhry.pomodojo.generated.resources.preferences_title
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.ui.components.PomodoroConfigSection
import com.fakhry.pomodojo.preferences.ui.components.PomodoroTimelinePreviewSection
import com.fakhry.pomodojo.preferences.ui.components.PreferenceAppearanceSection
import com.fakhry.pomodojo.preferences.ui.mapper.mapToTimelineSegmentsUi
import com.fakhry.pomodojo.preferences.ui.model.PreferenceOption
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Suppress("NonSkippableComposable")
@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = koinInject(),
    onNavigateBack: () -> Unit = {},
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                PreferencesContent(
                    state = state,
                    onThemeSelected = viewModel::onThemeSelected,
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
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondary)
            .windowInsetsPadding(WindowInsets.systemBars).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(
            onClick = {
                if (!isNavigatingBack) {
                    onNavigateBack()
                    isNavigatingBack = true
                }
            }) {
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
    state: PreferencesUiModel,
    onThemeSelected: (AppTheme) -> Unit = {},
    onRepeatCountChanged: (Int) -> Unit = {},
    onFocusSelected: (Int) -> Unit = {},
    onBreakSelected: (Int) -> Unit = {},
    onToggleLongBreak: (Boolean) -> Unit = {},
    onLongBreakAfterSelected: (Int) -> Unit = {},
    onLongBreakMinutesSelected: (Int) -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {

        PomodoroTimelinePreviewSection(
            segments = state.timelineSegments,
            hourSplits = state.timelineHourSplits,
        )

        Spacer(modifier = Modifier.height(32.dp))


        PomodoroConfigSection(
            state = state,
            onRepeatCountChanged = onRepeatCountChanged,
            onFocusSelected = onFocusSelected,
            onBreakSelected = onBreakSelected,
            onToggleLongBreak = onToggleLongBreak,
            onLongBreakAfterSelected = onLongBreakAfterSelected,
            onLongBreakMinutesSelected = onLongBreakMinutesSelected,
        )
        Spacer(modifier = Modifier.height(32.dp))

        PreferenceAppearanceSection(
            state = state,
            onOptionSelected = onThemeSelected,
        )
    }
}

@Preview
@Composable
private fun PreferencesContentPreview() {
    val preferences = PreferencesDomain(
        repeatCount = 4,
        focusMinutes = 25,
        breakMinutes = 5,
        longBreakEnabled = true,
        longBreakAfter = 4,
        longBreakMinutes = 10,
    )
    val previewPreferencesState = PreferencesUiModel(
        selectedTheme = preferences.appTheme,
        themeOptions = AppTheme.entries.map { theme ->
            PreferenceOption(
                label = theme.displayName,
                value = theme,
                selected = theme == preferences.appTheme,
            )
        }.toPersistentList(),
        repeatCount = preferences.repeatCount,
        focusOptions = listOf(10, 25, 50).map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = minutes == preferences.focusMinutes,
            )
        }.toPersistentList(),
        breakOptions = listOf(2, 5, 10).map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = minutes == preferences.breakMinutes,
            )
        }.toPersistentList(),
        isLongBreakEnabled = preferences.longBreakEnabled,
        longBreakAfterOptions = listOf(6, 4, 2).map { count ->
            PreferenceOption(
                label = "$count focuses",
                value = count,
                selected = count == preferences.longBreakAfter,
                enabled = preferences.longBreakEnabled,
            )
        }.toPersistentList(),
        longBreakOptions = listOf(4, 10, 20).map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = minutes == preferences.longBreakMinutes,
                enabled = preferences.longBreakEnabled,
            )
        }.toPersistentList(),
        timelineSegments = BuildFocusTimelineUseCase().invoke(preferences)
            .mapToTimelineSegmentsUi(),
        timelineHourSplits = BuildHourSplitTimelineUseCase().invoke(preferences).toPersistentList(),
        isLoading = false,
    )

    PomoDojoTheme {
        PreferencesContent(previewPreferencesState)
    }
}
