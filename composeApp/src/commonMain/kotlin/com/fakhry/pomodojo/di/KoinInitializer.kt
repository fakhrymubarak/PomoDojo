package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.core.database.di.databaseModule
import com.fakhry.pomodojo.core.datastore.di.dataStoreModule
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.dashboard.di.dashboardModule
import com.fakhry.pomodojo.features.focus.di.focusModule
import com.fakhry.pomodojo.features.preferences.di.preferencesModule
import org.koin.core.module.Module
import org.koin.dsl.module

private val appModule =
    module {
        single { DispatcherProvider() }
        databaseModule
    }

internal val composeAppModules: List<Module> =
    listOf(
        appModule,
        dataStoreModule,
        dashboardModule,
        preferencesModule,
        focusModule,
        // TODO: Move initialization to the focus screen
    )
