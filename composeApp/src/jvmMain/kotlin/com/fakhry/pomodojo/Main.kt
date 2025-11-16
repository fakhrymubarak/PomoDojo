package com.fakhry.pomodojo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fakhry.pomodojo.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.preferences.data.source.provideDataStore
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

fun main() = application {
    val preferences = loadDefaultPreferences()
    Window(
        onCloseRequest = ::exitApplication,
        title = "PomoDojo",
    ) {
        App(
            appTheme = preferences.appTheme,
            hasActiveSession = preferences.hasActiveSession,
        )
    }
}

private fun loadDefaultPreferences(): PreferencesDomain = runBlocking {
    runCatching {
        provideDataStore()
            .data
            .map { prefs ->
                PreferencesDomain(
                    appTheme = AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]),
                    hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                )
            }.first()
    }.getOrDefault(PreferencesDomain())
}
