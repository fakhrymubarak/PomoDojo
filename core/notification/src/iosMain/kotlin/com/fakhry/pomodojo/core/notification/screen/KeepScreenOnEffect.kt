package com.fakhry.pomodojo.core.notification.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOnEffect(enabled: Boolean) {
    DisposableEffect(enabled) {
        val application = UIApplication.sharedApplication
        application.idleTimerDisabled = enabled
        onDispose {
            application.idleTimerDisabled = false
        }
    }
}
