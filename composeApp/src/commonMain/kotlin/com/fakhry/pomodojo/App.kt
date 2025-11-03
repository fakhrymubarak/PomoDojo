package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.navigation.AppNavHost
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.AppTheme
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    KoinApplication(
        application = { modules(composeAppModules) }
    ) {
        val preferencesRepository: PreferencesRepository = koinInject()
        val preferences by preferencesRepository.preferences.collectAsState(PomodoroPreferences())
        val useDarkTheme = preferences.appTheme == AppTheme.DARK

        PomoDojoTheme(darkTheme = useDarkTheme) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}
