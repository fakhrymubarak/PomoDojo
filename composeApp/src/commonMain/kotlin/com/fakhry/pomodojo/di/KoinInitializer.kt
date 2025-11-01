package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.dashboard.di.dashboardModule
import com.fakhry.pomodojo.preferences.di.preferencesModule
import org.koin.core.module.Module

internal val composeAppModules: List<Module> = listOf(
    dashboardModule,
    preferencesModule,
)
