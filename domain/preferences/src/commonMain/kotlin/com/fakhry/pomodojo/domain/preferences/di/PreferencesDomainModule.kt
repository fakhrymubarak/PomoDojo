package com.fakhry.pomodojo.domain.preferences.di

import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val preferencesDomainModule = module {
    factoryOf(::PreferenceCascadeResolver)
}
