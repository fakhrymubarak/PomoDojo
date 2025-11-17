package com.fakhry.pomodojo.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fakhry.pomodojo.dashboard.ui.DashboardScreen
import com.fakhry.pomodojo.features.focus.ui.PomodoroCompleteScreen
import com.fakhry.pomodojo.features.focus.ui.PomodoroSessionScreen
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.preferences.ui.PreferencesRoute

private const val ANIMATION_DURATION = 500

@Suppress("NonSkippableComposable")
@Composable
fun AppNavHost(navController: NavHostController, hasActiveSession: Boolean = false) {
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

    val startDestination = if (hasActiveSession) {
        AppDestination.PomodoroSession
    } else {
        AppDestination.Dashboard
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                onSessionCompleted = { completion ->
                    navController.popBackStack()
                    if (completion.isEmpty()) return@PomodoroSessionScreen
                    navController.navigate(
                        AppDestination.PomodoroComplete(
                            totalCyclesFinished = completion.totalCyclesFinished,
                            totalFocusMinutes = completion.totalFocusMinutes,
                            totalBreakMinutes = completion.totalBreakMinutes,
                        ),
                    )
                },
            )
        }
        composable<AppDestination.PomodoroComplete> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<AppDestination.PomodoroComplete>()
            PomodoroCompleteScreen(
                uiState = PomodoroCompletionUiState(
                    totalCyclesFinished = args.totalCyclesFinished,
                    totalFocusMinutes = args.totalFocusMinutes,
                    totalBreakMinutes = args.totalBreakMinutes,
                ),
                onStartAnotherSession = { navController.popBackStack() },
            )
        }
    }
}
