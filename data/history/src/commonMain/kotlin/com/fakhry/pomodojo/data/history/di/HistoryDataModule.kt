package com.fakhry.pomodojo.data.history.di

import com.fakhry.pomodojo.data.history.repository.HistorySessionRepositoryImpl
import com.fakhry.pomodojo.domain.history.repository.HistorySessionRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val historyDataModule = module {
    singleOf(::HistorySessionRepositoryImpl) bind HistorySessionRepository::class
}
