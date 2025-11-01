package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.di.composeAppModules
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
            DashboardScreen(
                onStartPomodoro = { /* TODO: connect to timer screen */ },
                onOpenSettings = { /* TODO: navigate to preferences */ },
            )
        }
    }
}
