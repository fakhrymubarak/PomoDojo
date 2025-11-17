package com.fakhry.pomodojo.core.utils.permissions

import androidx.compose.runtime.Composable

fun interface NotificationPermissionRequester {
    fun requestPermission(onResult: (Boolean) -> Unit)
}

@Composable
expect fun rememberNotificationPermissionRequester(): NotificationPermissionRequester
