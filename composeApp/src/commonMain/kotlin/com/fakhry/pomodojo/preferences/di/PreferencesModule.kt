package com.fakhry.pomodojo.preferences.di

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val preferencesModule = module {
    viewModelOf(::PreferencesViewModel)
    factoryOf(::PreferenceCascadeResolver)
    factoryOf(::BuildFocusTimelineUseCase)
    singleOf(::PreferencesRepository)
    factory<PreferencesValidator> { PreferencesValidator }
}
