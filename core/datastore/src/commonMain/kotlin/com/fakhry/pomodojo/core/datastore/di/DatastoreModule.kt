package com.fakhry.pomodojo.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.fakhry.pomodojo.core.datastore.DataStorePreferenceStorage
import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.core.datastore.provideDataStore
import org.koin.dsl.module

val dataStoreModule = module {
    single<DataStore<Preferences>> { provideDataStore() }
    single<PreferenceStorage> { DataStorePreferenceStorage(get()) }
}
