package com.fakhry.pomodojo.feature.notification.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOnEffect(keepScreenOn: Boolean) {
    DisposableEffect(keepScreenOn) {
        val application = UIApplication.sharedApplication()
        val previous = application.isIdleTimerDisabled()
        application.setIdleTimerDisabled(keepScreenOn)
        onDispose {
            application.setIdleTimerDisabled(previous)
        }
    }
}
