package com.fakhry.pomodojo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fakhry.pomodojo.app.di.getAppModules
import org.koin.compose.KoinApplication

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PomoDojo",
    ) {
        KoinApplication(application = {
            modules(getAppModules())
        }) {
            App()
        }
    }
}
