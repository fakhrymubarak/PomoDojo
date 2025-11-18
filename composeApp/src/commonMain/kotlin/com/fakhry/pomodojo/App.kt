package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.core.datastore.provideDataStore
import com.fakhry.pomodojo.core.navigation.AppNavHost
import com.fakhry.pomodojo.core.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.features.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.usecase.InitPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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

/**
 * This code should run once on MainThread to load the AppTheme and StartDestination.
 * */
private fun getInitPreferencesOnMainThread(): InitAppPreferences {
    val preferences = runBlocking {
        runCatching {
            provideDataStore().data.map { prefs ->
                // Take only initial data for more optimal processing
                InitAppPreferences(
                    appTheme = AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]),
                    hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                )
            }.first()
        }.getOrDefault(InitAppPreferences())
    }
    return preferences
}
