package com.fakhry.pomodojo.dashboard.di

import com.fakhry.pomodojo.dashboard.data.repository.PomodoroHistoryRepositoryImpl
import com.fakhry.pomodojo.dashboard.domain.repository.PomodoroHistoryRepository
import com.fakhry.pomodojo.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule =
    module {
        viewModelOf(::DashboardViewModel)
        factory<PomodoroHistoryRepository> {
            val database: PomoDojoRoomDatabase = get()
            PomodoroHistoryRepositoryImpl(database.historySessionDao())
        }
    }
