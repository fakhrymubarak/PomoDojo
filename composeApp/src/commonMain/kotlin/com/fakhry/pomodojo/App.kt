package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.core.datastore.wiring.getInitPreferencesOnMainThread
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import com.fakhry.pomodojo.core.navigation.AppNavHost
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.repository.InitPreferencesRepository
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App(onThemeUpdated: (AppTheme) -> Unit = {}) {
    val initialPrefs = remember { getInitPreferencesOnMainThread() }

    KoinApplication(
        application = { modules(composeAppModules) },
    ) {
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
}
