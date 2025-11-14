package com.fakhry.pomodojo.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester = remember {
    val scope = MainScope()
    NotificationPermissionRequester { onResult ->
        scope.launch {
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionBadge or
                    UNAuthorizationOptionSound,
                completionHandler = { granted, error ->
                    error?.let {
                        println(
                            "Notification permission request failed: ${it.localizedDescription}",
                        )
                    }
                    onResult(granted)
                },
            )
        }
    }
}
