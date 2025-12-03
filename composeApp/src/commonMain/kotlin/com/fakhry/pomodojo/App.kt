package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import com.fakhry.pomodojo.core.navigation.AppNavHost
import com.fakhry.pomodojo.features.preferences.data.getInitPreferencesOnMainThread
import com.fakhry.pomodojo.features.preferences.domain.repository.InitPreferencesRepository
import org.koin.compose.koinInject

@Composable
fun App(onThemeUpdated: (String) -> Unit = {}) {
    val initialPrefs = remember { getInitPreferencesOnMainThread() }

    val initPreferencesRepository = koinInject<InitPreferencesRepository>()
    val initPreferences by initPreferencesRepository.initPreferences.collectAsState(
        initial = initialPrefs,
    )
    val appTheme by remember { derivedStateOf { initPreferences.appTheme } }

    // Run side-effect only when theme actually changes
    LaunchedEffect(appTheme) {
        onThemeUpdated(appTheme)
    }

    PomoDojoTheme(appTheme) {
        val navController = rememberNavController()
        AppNavHost(
            hasActiveSession = initialPrefs.hasActiveSession,
            navController = navController,
        )
    }
}
