package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.navigation.AppNavHost
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App(
    appTheme: AppTheme = AppTheme.DARK,
    hasActiveSession: Boolean = false,
    onThemeUpdated: (AppTheme) -> Unit = {},
) {
    KoinApplication(
        application = { modules(composeAppModules) },
    ) {
        val appThemeState = remember { mutableStateOf(appTheme) }
        val preferencesRepository = koinInject<PreferencesRepository>()
        val preferences by preferencesRepository.preferences.collectAsState(initial = null)

        if (preferences != null && preferences!!.appTheme != appThemeState.value) {
            appThemeState.value = preferences!!.appTheme
            onThemeUpdated(appThemeState.value)
        }

        PomoDojoTheme(appThemeState.value) {
            val navController = rememberNavController()
            AppNavHost(
                hasActiveSession = hasActiveSession,
                navController = navController,
            )
        }
    }
}
