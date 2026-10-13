package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import com.fakhry.pomodojo.core.notification.R as RP

private const val PIC_KEY_ICON = "pomodojo_icon"
private const val ISLAND_CHANNEL_ID = "focus"
private const val ISLAND_CHANNEL_NAME = "Focus"

private const val NOTIF_GROUP_DYNAMIC_ISLAND = "dynamic_island"
private const val EXTRA_MIUI_FOCUS_PARAM = "miui.focus.param"

/**
 * Xiaomi Dynamic Island Notification Implementation
 *
 * */
class XiaomiNotifManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) : NotifManager {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun notify(notice: SessionNotice.Showing) {
        notifyWithDynamicIsland(
            intent = context.launchActivityPendingIntent(),
            notice = notice,
            notifId = dynamicIslandNotificationId(notice.sessionId),
        )
    }

    /**
     * Cancel notification dynamic island for xiaomi.
     * */
    override fun cancel(sessionId: String) {
        notificationManager.cancel(dynamicIslandNotificationId(sessionId))
    }

    /**
     * Register notification dynamic island for xiaomi.
     * */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifyWithDynamicIsland(
        intent: PendingIntent,
        notice: SessionNotice.Showing,
        notifId: Int,
    ) {
        val title = notice.title(context)
        val picture = HyperPicture(PIC_KEY_ICON, context, RP.drawable.ic_timer)
        val hyperIslandBuilder =
            HyperIslandNotification.Builder(context, ISLAND_CHANNEL_ID, ISLAND_CHANNEL_NAME)
                .setSmallWindowTarget("${context.packageName}.MainActivity")
                .setSmallIslandIcon(PIC_KEY_ICON)
                .addPicture(picture)

        if (notice is SessionNotice.Running) {
            // Show Timer on Dynamic Island
            val countdownTimer = TimerInfo(
                -1,
                notice.endsAtMs,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
            )
            hyperIslandBuilder
                .setChatInfo(title = title, timer = countdownTimer, pictureKey = PIC_KEY_ICON)
                .setBigIslandCountdown(notice.endsAtMs, PIC_KEY_ICON)
        } else {
            // Paused or at the Gate nothing counts down, so the island shows static text.
            hyperIslandBuilder
                .setChatInfo(
                    title = title,
                    content = notice.status(context),
                    pictureKey = PIC_KEY_ICON,
                )
        }

        // Show Timer on Notification
        val notification = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(RP.drawable.ic_timer)
            .setContentTitle(title)
            .addExtras(hyperIslandBuilder.buildResourceBundle())
            .setContentIntent(intent)
            .setGroup(NOTIF_GROUP_DYNAMIC_ISLAND).build()
        notification.extras.putString(EXTRA_MIUI_FOCUS_PARAM, hyperIslandBuilder.buildJsonParam())

        notificationManager.notify(notifId, notification)
    }

    private fun dynamicIslandNotificationId(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + NOTIF_DYNAMIC_ISLAND_ID_OFFSET + sessionId.hashCode()
}
