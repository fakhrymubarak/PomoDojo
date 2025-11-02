package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.navigation.AppNavHost
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(
        application = { modules(composeAppModules) }
    ) {
        PomoDojoTheme {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}