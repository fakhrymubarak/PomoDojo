package com.fakhry.pomodojo.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.focus.ui.PomodoroSessionScreen
import com.fakhry.pomodojo.preferences.ui.PreferencesRoute
import kotlinx.serialization.Serializable

object AppDestination {
    @Serializable
    data object Dashboard

    @Serializable
    data object Preferences

    @Serializable
    data object PomodoroSession
}

private const val ANIMATION_DURATION = 500

@Suppress("NonSkippableComposable")
@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    val enterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(ANIMATION_DURATION),
    )
    val exitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(ANIMATION_DURATION),
    )
    val popEnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(ANIMATION_DURATION),
    )
    val popExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(ANIMATION_DURATION),
    )

    NavHost(
        navController = navController,
        startDestination = AppDestination.Dashboard,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition },
    ) {
        composable<AppDestination.Dashboard> {
            DashboardScreen(
                onStartPomodoro = {
                    navController.navigate(AppDestination.PomodoroSession) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = {
                    navController.navigate(AppDestination.Preferences) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<AppDestination.Preferences> {
            PreferencesRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<AppDestination.PomodoroSession> {
            PomodoroSessionScreen(
                onSessionCompleted = { navController.popBackStack() },
            )
        }
    }
}
