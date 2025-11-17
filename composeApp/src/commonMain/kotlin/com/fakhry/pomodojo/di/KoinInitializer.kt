package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.core.datastore.provideDataStore
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.dashboard.di.dashboardModule
import com.fakhry.pomodojo.features.focus.di.focusModule
import com.fakhry.pomodojo.features.preferences.data.source.DataStorePreferenceStorage
import com.fakhry.pomodojo.features.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.di.preferencesModule
import org.koin.core.module.Module
import org.koin.dsl.module

private val appModule =
    module {
        single { DispatcherProvider() }
        single<PreferenceStorage> { DataStorePreferenceStorage(provideDataStore()) }
    }

internal val composeAppModules: List<Module> =
    listOf(
        appModule,
        dashboardModule,
        preferencesModule,
        focusModule,
        // TODO: Move initialization to the focus screen
    )
