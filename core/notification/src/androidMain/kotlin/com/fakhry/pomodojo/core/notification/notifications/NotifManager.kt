package com.fakhry.pomodojo.core.notification.notifications

import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary

internal const val NOTIF_REQUEST_CODE_OFFSET = 42_000
internal const val NOTIF_CHANNEL_ID = "focus_session_channel"
internal const val NOTIF_CHANNEL_NAME = "Focus Sessions"
internal const val NOTIF_CHANNEL_DESC = "Focus session progress"

interface NotifManager {
    fun notify(summary: NotificationSummary)

    fun cancel(sessionId: String)
}
