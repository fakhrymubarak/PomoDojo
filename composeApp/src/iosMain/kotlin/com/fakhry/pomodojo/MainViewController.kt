package com.fakhry.pomodojo

import androidx.compose.ui.window.ComposeUIViewController
import com.fakhry.pomodojo.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.preferences.data.source.provideDataStore
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    val preferences = loadIosPreferences()
    return ComposeUIViewController {
        App(
            appTheme = preferences.appTheme,
            hasActiveSession = preferences.hasActiveSession,
        )
    }
}

private fun loadIosPreferences(): PreferencesDomain = runBlocking {
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
