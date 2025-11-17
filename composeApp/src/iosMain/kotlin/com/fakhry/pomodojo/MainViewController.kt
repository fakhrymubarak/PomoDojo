package com.fakhry.pomodojo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming", "FunctionName", "unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}
