package com.fakhry.pomodojo.core.designsystem.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.setStatusBarHidden

@Composable
actual fun ImmersiveModeEffect() {
    DisposableEffect(Unit) {
        // Ask the root view controller to hide the status bar
        val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVc?.setNeedsStatusBarAppearanceUpdate()
        UIApplication.sharedApplication.setStatusBarHidden(hidden = false, animated = false)

        onDispose {
            UIApplication.sharedApplication.setStatusBarHidden(hidden = false, animated = false)
        }
    }
}
