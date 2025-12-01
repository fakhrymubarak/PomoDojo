package com.fakhry.pomodojo.app.di

import com.fakhry.pomodojo.core.database.di.databaseModule
import com.fakhry.pomodojo.core.datastore.di.dataStoreModule
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.data.history.di.historyDataModule
import com.fakhry.pomodojo.data.pomodoro.di.pomodoroDataModule
import com.fakhry.pomodojo.data.preferences.di.preferencesDataModule
import com.fakhry.pomodojo.domain.pomodoro.di.focusDomainModule
import com.fakhry.pomodojo.domain.preferences.di.preferencesDomainModule
import com.fakhry.pomodojo.features.dashboard.di.dashboardFeatureModule
import com.fakhry.pomodojo.features.focus.di.pomodoroFeatureModule
import com.fakhry.pomodojo.features.preferences.di.preferencesFeatureModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun getAppModules(): List<Module> = listOf(
    appModule,
    *dataModule.toTypedArray(),
    *domainModule.toTypedArray(),
    *featuresModule.toTypedArray(),
    *coreModule.toTypedArray(),
)

private val appModule = module {
    single { DispatcherProvider() }
}
private val featuresModule = listOf(
    dashboardFeatureModule,
    pomodoroFeatureModule,
    preferencesFeatureModule,
)

private val domainModule: List<Module> = listOf(
    focusDomainModule,
    preferencesDomainModule,
)

private val dataModule: List<Module> = listOf(
    historyDataModule,
    preferencesDataModule,
    pomodoroDataModule,
)

private val coreModule: List<Module> = listOf(
    dataStoreModule,
    databaseModule,
)
