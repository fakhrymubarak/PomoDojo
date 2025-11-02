package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.preferences.PreferencesScreen
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    PlatformKoinInitializer()
    KoinApplication(application = {
        modules(composeAppModules)
    }) {
        PomoDojoTheme {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = AppScreen.Dashboard.route,
            ) {
                composable(AppScreen.Dashboard.route) {
                    DashboardScreen(
                        onStartPomodoro = { /* TODO: connect to timer screen */ },
                        onOpenSettings = {
                            navController.navigate(AppScreen.Preferences.route)
                        },
                    )
                }
                composable(AppScreen.Preferences.route) {
                    PreferencesScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private enum class AppScreen(val route: String) {
    Dashboard("dashboard"),
    Preferences("preferences"),
}

@Composable
internal expect fun PlatformKoinInitializer()
