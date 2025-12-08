package com.fakhry.pomodojo.features.preferences.di

import com.fakhry.pomodojo.features.preferences.ui.PreferencesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val preferencesFeatureModule = module {
    viewModelOf(::PreferencesViewModel)
}
