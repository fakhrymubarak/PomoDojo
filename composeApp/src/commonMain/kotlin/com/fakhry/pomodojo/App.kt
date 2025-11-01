package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    PomoDojoTheme {
        DashboardScreen(
            onStartPomodoro = { /* TODO: connect to timer screen */ },
            onOpenSettings = { /* TODO: navigate to preferences */ },
        )
    }
}