package com.fakhry.pomodojo

import android.app.Application
import com.fakhry.pomodojo.app.di.androidKoinAppModule
import com.fakhry.pomodojo.app.di.getAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PomoDojoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PomoDojoApp)
            modules(listOf(androidKoinAppModule()) + getAppModules())
        }
    }
}
