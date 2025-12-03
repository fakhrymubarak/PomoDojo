package com.fakhry.pomodojo

import androidx.compose.ui.window.ComposeUIViewController
import com.fakhry.pomodojo.app.di.getAppModules
import org.koin.compose.KoinApplication
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming", "FunctionName", "unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    KoinApplication(application = {
        modules(getAppModules())
    }) {
        App()
    }
}
