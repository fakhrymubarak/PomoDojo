package com.fakhry.pomodojo

import android.app.Application
import com.fakhry.pomodojo.focus.data.db.AndroidAppDependenciesInitializer

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
