package com.fakhry.pomodojo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fakhry.pomodojo.app.di.getAppModules
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(getAppModules())
        }
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "PomoDojo",
    ) {
        App()
    }
}
