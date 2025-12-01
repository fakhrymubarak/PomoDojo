package com.fakhry.pomodojo.data.preferences.di

import com.fakhry.pomodojo.data.preferences.repository.PreferencesRepositoryImpl
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesDataModule = module {
    singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
}
