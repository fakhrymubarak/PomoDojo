package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.core.database.di.databaseModule
import com.fakhry.pomodojo.core.datastore.di.dataStoreModule
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.data.history.di.historyDataModule
import com.fakhry.pomodojo.features.dashboard.di.dashboardModule
import com.fakhry.pomodojo.features.focus.di.focusModule
import com.fakhry.pomodojo.features.preferences.di.preferencesModule
import org.koin.core.module.Module
import org.koin.dsl.module

private val appModule = module {
    single { DispatcherProvider() }
}

// Should only contains features dependency
internal val composeAppModules: List<Module> = listOf(
    databaseModule,
    historyDataModule,
    appModule,
    databaseModule,
    dataStoreModule,
    dashboardModule,
    focusModule,
) + preferencesModule
