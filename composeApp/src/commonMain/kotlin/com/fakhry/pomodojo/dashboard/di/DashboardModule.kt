package com.fakhry.pomodojo.dashboard.di

import com.fakhry.pomodojo.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.repository.HistorySessionRepositoryImpl
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule =
    module {
        viewModelOf(::DashboardViewModel)
        factory<HistorySessionRepository> {
            val database: PomoDojoRoomDatabase = get()
            HistorySessionRepositoryImpl(database.historySessionDao())
        }
    }
