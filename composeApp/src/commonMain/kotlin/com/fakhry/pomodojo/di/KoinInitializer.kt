package com.fakhry.pomodojo.di

import com.fakhry.pomodojo.dashboard.di.dashboardModule
import com.fakhry.pomodojo.preferences.data.source.DataStorePreferenceStorage
import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.data.source.provideDataStore
import com.fakhry.pomodojo.preferences.di.preferencesModule
import com.fakhry.pomodojo.utils.DispatcherProvider
import org.koin.core.module.Module
import org.koin.dsl.module

private val appModule = module {
    single { DispatcherProvider() }
    single<PreferenceStorage> { DataStorePreferenceStorage(provideDataStore()) }
}

internal val composeAppModules: List<Module> = listOf(
    appModule,
    dashboardModule,
    preferencesModule,
)
