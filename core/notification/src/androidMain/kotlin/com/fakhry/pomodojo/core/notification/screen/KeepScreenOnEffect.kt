package com.fakhry.pomodojo.core.notification.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled, view) {
        val window = view.context.findActivity()?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = window?.attributes?.flags

        if (enabled) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            insetsController?.isAppearanceLightStatusBars = false
        } else {
            if (previous != null) {
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        onDispose {
            if (enabled) {
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}

private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
