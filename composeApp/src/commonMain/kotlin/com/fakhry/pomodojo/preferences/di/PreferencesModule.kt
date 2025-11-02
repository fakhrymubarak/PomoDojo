package com.fakhry.pomodojo.preferences.di

import com.fakhry.pomodojo.preferences.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.PreferencesRepository
import com.fakhry.pomodojo.preferences.PreferencesValidator
import com.fakhry.pomodojo.preferences.PreferencesViewModel
import com.fakhry.pomodojo.preferences.TimelinePreviewBuilder
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val preferencesModule = module {
    viewModelOf(::PreferencesViewModel)
    factoryOf(::PreferenceCascadeResolver)
    factoryOf(::TimelinePreviewBuilder)
    singleOf(::PreferencesRepository)
    factory<PreferencesValidator> { PreferencesValidator }
}
