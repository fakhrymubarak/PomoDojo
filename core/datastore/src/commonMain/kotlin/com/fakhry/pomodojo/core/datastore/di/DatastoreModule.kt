package com.fakhry.pomodojo.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.fakhry.pomodojo.core.datastore.createDatastore
import org.koin.dsl.module

val dataStoreModule = module {
    single<DataStore<Preferences>> { createDatastore() }
}
