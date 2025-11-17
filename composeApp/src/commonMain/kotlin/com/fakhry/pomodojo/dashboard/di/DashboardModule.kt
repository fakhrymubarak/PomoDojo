package com.fakhry.pomodojo.dashboard.di

import com.fakhry.pomodojo.core.database.PomoDojoRoomDatabase
import com.fakhry.pomodojo.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.focus.data.repository.HistorySessionRepositoryImpl
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
    single<HistorySessionRepository> {
        HistorySessionRepositoryImpl(get<PomoDojoRoomDatabase>().historySessionDao(), get())
    }
}
