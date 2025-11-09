package com.fakhry.pomodojo.dashboard.di

import com.fakhry.pomodojo.dashboard.viewmodel.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule =
    module {
        viewModelOf(::DashboardViewModel)
    }
