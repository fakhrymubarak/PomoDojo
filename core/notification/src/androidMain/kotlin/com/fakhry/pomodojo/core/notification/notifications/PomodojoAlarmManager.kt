package com.fakhry.pomodojo.core.notification.notifications

import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary

interface PomodojoAlarmManager {
    fun schedule(summary: NotificationSummary)

    fun cancel(sessionId: String)
}
