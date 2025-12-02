package com.fakhry.pomodojo.app.di

import com.fakhry.pomodojo.core.database.AndroidFocusDatabaseHolder
import com.fakhry.pomodojo.core.datastore.AndroidDataStoreProvider
import com.fakhry.pomodojo.feature.notification.audio.AndroidSoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidKoinAppModule(): Module = module {
    single { AndroidDataStoreProvider(androidContext()) }
    single { AndroidFocusDatabaseHolder(androidContext()) }
    single { AndroidSoundPlayer(androidContext()) }
}
