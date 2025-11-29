package com.fakhry.pomodojo.core.framework.screen

import androidx.compose.runtime.Composable

@Composable
actual fun KeepScreenOnEffect(keepScreenOn: Boolean) {
    // Desktop currently has no screen-on concept; no-op.
}
