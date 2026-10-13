package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.R
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice

private const val MAX_PROGRESS = 100

class DefaultPomodojoNotifManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) : NotifManager {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun notify(notice: SessionNotice.Showing) {
        ensureChannel()

        val title = notice.title(context)
        val builder = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(MAX_PROGRESS, notice.progressPercent(), false)
            .setContentIntent(context.launchActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_MAX)

        // Use chronometer for self-updating timer while running
        if (notice is SessionNotice.Running) {
            builder
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setShowWhen(true)
                .setWhen(notice.endsAtMs)
        } else {
            builder.setShowWhen(false)
        }
        val body = if (notice is SessionNotice.AtGate) {
            notice.status(context)
        } else {
            notice.quote.withAttribution()
        }

        builder.setStyle(
            NotificationCompat
                .BigTextStyle()
                .setBigContentTitle(title)
                .bigText(body),
        )

        notificationManager.notify(sessionNotificationId(notice.sessionId), builder.build())
    }

    override fun cancel(sessionId: String) {
        notificationManager.cancel(sessionNotificationId(sessionId))
    }

    private fun ensureChannel() {
        if (notificationManager.getNotificationChannel(NOTIF_CHANNEL_ID) == null) {
            val channel = NotificationChannelCompat.Builder(
                NOTIF_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT,
            ).setName(NOTIF_CHANNEL_NAME)
                .setDescription(NOTIF_CHANNEL_DESC)
                .setSound(null, null)
                .setVibrationEnabled(false)
                .build()
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sessionNotificationId(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + sessionId.hashCode()
}
