package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.preferences.PreferencesScreen
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(composeAppModules)
    }) {
        PomoDojoTheme {
            var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }

            when (currentScreen) {
                AppScreen.Dashboard -> DashboardScreen(
                    onStartPomodoro = { /* TODO: connect to timer screen */ },
                    onOpenSettings = { currentScreen = AppScreen.Preferences },
                )
                AppScreen.Preferences -> PreferencesScreen(
                    onNavigateBack = { currentScreen = AppScreen.Dashboard },
                )
            }
        }
    }
}

private enum class AppScreen {
    Dashboard,
    Preferences,
}
