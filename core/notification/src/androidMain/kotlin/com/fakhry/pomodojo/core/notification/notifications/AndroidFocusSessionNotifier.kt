package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import org.koin.core.context.GlobalContext

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier {
    val koin = GlobalContext.get()
    val notifier: AndroidFocusSessionNotifier = koin.get()
    return notifier
}

class AndroidFocusSessionNotifier(
    private val context: Context,
    private val notifManager: NotifManager,
    private val pomodojoAlarmManager: PomodojoAlarmManager,
) : PomodoroSessionNotifier {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun schedule(snapshot: PomodoroSessionDomain) {
        val now = System.currentTimeMillis()
        val summary = snapshot.toNotificationSummary(context, now)
        notifManager.notify(summary)
        pomodojoAlarmManager.schedule(summary)
    }

    override suspend fun cancel(sessionId: String) {
        notifManager.cancel(sessionId)
        pomodojoAlarmManager.cancel(sessionId)
    }
}
