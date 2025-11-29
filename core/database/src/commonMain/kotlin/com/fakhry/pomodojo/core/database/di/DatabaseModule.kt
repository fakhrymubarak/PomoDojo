package com.fakhry.pomodojo.core.database.di

import com.fakhry.pomodojo.core.database.PomoDojoRoomDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        get<PomoDojoRoomDatabase>().historySessionDao()
    }
}
