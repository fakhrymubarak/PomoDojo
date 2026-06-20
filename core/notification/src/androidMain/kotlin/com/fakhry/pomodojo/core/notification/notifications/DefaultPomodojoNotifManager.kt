package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.R
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.domain.pomodoro.model.notification.CompletionNotificationSummary
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary
import com.fakhry.pomodojo.core.notification.R as RN

private const val MAX_PROGRESS = 100

class DefaultPomodojoNotifManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) : NotifManager {

    override fun notify(summary: NotificationSummary) {
        ensureChannel()

        val builder = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setContentTitle(summary.title)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(MAX_PROGRESS, summary.segmentProgressPercent, false)
            .setContentIntent(context.launchActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_MAX)

        // Use chronometer for self-updating timer when not paused
        if (!summary.isPaused && summary.finishTimeMillis > 0) {
            builder
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setShowWhen(true)
                .setWhen(summary.finishTimeMillis)
        } else {
            // For paused state, show static time text
            builder.setShowWhen(false)
        }

        builder.setStyle(
            NotificationCompat
                .BigTextStyle()
                .setBigContentTitle(summary.title)
                .bigText(summary.quote),
        )

        notificationManager.notify(sessionNotificationId(summary.sessionId), builder.build())

        // Schedule cancel notification if all segments completed
//        if (summary.isAllSegmentsCompleted) scheduleCompletion(snapshot.toCompletionSummary())
    }

    override fun cancel(sessionId: String) {
        notificationManager.cancel(sessionNotificationId(sessionId))
    }

    // Not wired up yet: planned completion notification (see commented call in notify())
    @Suppress("UnusedPrivateMember")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun scheduleCompletion(summary: CompletionNotificationSummary) {
        notificationManager.cancel(sessionNotificationId(summary.sessionId))
        val title =
            context.getString(RN.string.focus_session_complete_title)
        val body = context.getString(
            RN.string.focus_session_complete_body,
            summary.completedCycles,
            summary.totalFocusMinutes,
            summary.totalBreakMinutes,
        )

        val builder = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(context.launchActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(body),
            )
        notificationManager.notify(completedNotificationId(summary.sessionId), builder.build())
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

    private fun completedNotificationId(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + NOTIF_COMPLETED_ID_OFFSET + sessionId.hashCode()
}
