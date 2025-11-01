package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.dashboard.di.dashboardModule
import org.koin.core.module.Module

internal val composeAppModules: List<Module> = listOf(
    dashboardModule,
)