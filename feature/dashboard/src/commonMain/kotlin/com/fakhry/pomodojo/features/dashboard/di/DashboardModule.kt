package com.fakhry.pomodojo.features.dashboard.di

import com.fakhry.pomodojo.features.dashboard.ui.viewmodel.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
}
