package com.fakhry.pomodojo.core.utils.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester =
    remember { NotificationPermissionRequester { onResult -> onResult(true) } }
