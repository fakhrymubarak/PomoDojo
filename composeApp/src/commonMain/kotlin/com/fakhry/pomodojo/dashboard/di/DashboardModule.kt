package com.fakhry.pomodojo.dashboard.di

import com.fakhry.pomodojo.dashboard.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.dashboard.viewmodel.PomodoroHistoryRepository
import com.fakhry.pomodojo.dashboard.viewmodel.PomodoroHistoryRepositoryImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dashboardModule =
    module {
        viewModelOf(::DashboardViewModel)
        factoryOf(::PomodoroHistoryRepositoryImpl) bind PomodoroHistoryRepository::class
    }
