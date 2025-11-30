package com.fakhry.pomodojo.features.preferences.di

import com.fakhry.pomodojo.data.preferences.di.preferencesDataModule
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.features.preferences.ui.PreferencesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val preferencesFeatureModule = module {
    viewModelOf(::PreferencesViewModel)
    factoryOf(::PreferenceCascadeResolver)
    factoryOf(::BuildTimerSegmentsUseCase)
    factoryOf(::BuildHourSplitTimelineUseCase)
}

val preferencesModule = listOf(
    preferencesDataModule,
    preferencesFeatureModule,
)
