package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.navigation.AppNavHost
import com.fakhry.pomodojo.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.preferences.data.source.provideDataStore
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App(onThemeUpdated: (AppTheme) -> Unit = {}) {
    val initialPreferences = remember { getPreferencesOnMainThread() }

    KoinApplication(
        application = { modules(composeAppModules) },
    ) {
        val appThemeState = remember { mutableStateOf(initialPreferences.appTheme) }
        val preferencesRepository = koinInject<PreferencesRepository>()
        val preferences by preferencesRepository.preferences.collectAsState(
            initial = initialPreferences,
        )

        if (appThemeState.value != preferences.appTheme) {
            appThemeState.value = preferences.appTheme
            onThemeUpdated(appThemeState.value)
        }

        PomoDojoTheme(appThemeState.value) {
            val navController = rememberNavController()
            AppNavHost(
                hasActiveSession = preferences.hasActiveSession,
                navController = navController,
            )
        }
    }
}

/**
 * This code should run once on MainThread to load the AppTheme and StartDestination.
 * */
private fun getPreferencesOnMainThread(): PreferencesDomain {
    val preferences = runBlocking {
        runCatching {
            provideDataStore().data.map { prefs ->
                // Take only initial data for more optimal processing
                PreferencesDomain(
                    appTheme = AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]),
                    hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                )
            }.first()
        }.getOrDefault(PreferencesDomain())
    }
    return preferences
}
