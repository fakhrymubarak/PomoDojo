package com.fakhry.pomodojo.core.notification.notifications

interface PomodojoAlarmManager {
    fun schedule(sessionId: String, endsAtMs: Long)

    fun cancel(sessionId: String)
}
