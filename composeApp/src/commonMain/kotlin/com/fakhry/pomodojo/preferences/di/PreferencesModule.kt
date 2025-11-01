package com.fakhry.pomodojo.preferences.di

import com.fakhry.pomodojo.preferences.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.PreferenceStorage
import com.fakhry.pomodojo.preferences.PreferencesRepository
import com.fakhry.pomodojo.preferences.PreferencesViewModel
import com.fakhry.pomodojo.preferences.TimelinePreviewBuilder
import com.fakhry.pomodojo.preferences.platformPreferenceStorage
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val preferencesModule = module {
    single<PreferenceStorage> { platformPreferenceStorage() }
    singleOf(::PreferenceCascadeResolver)
    singleOf(::TimelinePreviewBuilder)
    single { PreferencesRepository(get(), get()) }
    viewModelOf(::PreferencesViewModel)
}
