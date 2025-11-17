package com.fakhry.pomodojo

import android.app.Application

class PomoDojoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidAppDependenciesInitializer.initialize(applicationContext)
    }

    override fun onTerminate() {
        AndroidAppDependenciesInitializer.destroy()
        super.onTerminate()
    }
}
