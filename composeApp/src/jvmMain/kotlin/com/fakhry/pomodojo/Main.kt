package com.fakhry.pomodojo

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fakhry.pomodojo.app.di.getAppModules
import org.koin.compose.KoinApplication

fun main() = application {
    val windowState = rememberWindowState(
        width = 400.dp,
        height = 1500.dp,
        position = WindowPosition.Aligned(alignment = Alignment.TopEnd),
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "PomoDojo",
        state = windowState,
    ) {
        KoinApplication(application = {
            modules(getAppModules())
        }) {
            App()
        }
    }
}
