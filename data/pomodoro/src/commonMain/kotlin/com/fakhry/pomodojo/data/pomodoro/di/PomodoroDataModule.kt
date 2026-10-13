package com.fakhry.pomodojo.data.pomodoro.di

import com.fakhry.pomodojo.data.pomodoro.repository.ActiveSessionStoreImpl
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val pomodoroDataModule = module {
    singleOf(::ActiveSessionStoreImpl) bind ActiveSessionStore::class
}
