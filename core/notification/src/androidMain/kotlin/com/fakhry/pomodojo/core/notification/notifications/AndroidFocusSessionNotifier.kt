package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import androidx.annotation.RequiresPermission
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice
import org.koin.core.context.GlobalContext

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier {
    val koin = GlobalContext.get()
    val notifier: AndroidFocusSessionNotifier = koin.get()
    return notifier
}

class AndroidFocusSessionNotifier(
    private val notifManager: NotifManager,
    private val pomodojoAlarmManager: PomodojoAlarmManager,
) : PomodoroSessionNotifier {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun show(notice: SessionNotice) {
        when (notice) {
            is SessionNotice.Showing -> notifManager.notify(notice)
            is SessionNotice.Cleared -> notifManager.cancel(notice.sessionId)
        }
        // Only a running Segment has an end to wake up for.
        if (notice is SessionNotice.Running) {
            pomodojoAlarmManager.schedule(notice.sessionId, notice.endsAtMs)
        } else {
            pomodojoAlarmManager.cancel(notice.sessionId)
        }
    }
}
