package com.fakhry.pomodojo.data.pomodoro.di

import com.fakhry.pomodojo.data.pomodoro.wiring.activeSessionStoreFactory
import org.koin.dsl.module

val pomodoroDataModule = module {
    single { activeSessionStoreFactory() }
}
