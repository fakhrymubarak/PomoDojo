package com.fakhry.pomodojo.core.database.di

import com.fakhry.pomodojo.core.database.PomoDojoRoomDatabase
import com.fakhry.pomodojo.core.database.createDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<PomoDojoRoomDatabase> { createDatabase() }
    single { get<PomoDojoRoomDatabase>().historySessionDao() }
}
