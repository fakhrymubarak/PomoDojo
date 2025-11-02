package com.fakhry.pomodojo.preferences.di

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import com.fakhry.pomodojo.preferences.domain.TimelinePreviewBuilder
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
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
