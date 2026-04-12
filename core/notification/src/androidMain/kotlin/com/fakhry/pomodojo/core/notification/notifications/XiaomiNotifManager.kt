package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary
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
    override fun notify(summary: NotificationSummary) {
        notifyWithDynamicIsland(
            intent = pendingActivityIntent(),
            summary = summary,
            notifId = dynamicIslandNotificationId(summary.sessionId),
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
        summary: NotificationSummary,
        notifId: Int,
    ) {
        val countdownTimer = TimerInfo(
            -1,
            summary.finishTimeMillis,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
        )

        // Show Timer on Dynamic Island
        val picture = HyperPicture(PIC_KEY_ICON, context, RP.drawable.ic_timer)
        val hyperIslandBuilder =
            HyperIslandNotification.Builder(context, ISLAND_CHANNEL_ID, ISLAND_CHANNEL_NAME)
                .setSmallWindowTarget("${context.packageName}.MainActivity")
                .setChatInfo(
                    title = summary.title,
                    timer = countdownTimer,
                    pictureKey = PIC_KEY_ICON
                )
                .setBigIslandCountdown(summary.finishTimeMillis, PIC_KEY_ICON)
                .setSmallIslandIcon(PIC_KEY_ICON)
                .addPicture(picture)

        // Show Timer on Notification
        val notification = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(RP.drawable.ic_timer)
            .setContentTitle(summary.title)
            .addExtras(hyperIslandBuilder.buildResourceBundle())
            .setContentIntent(intent)
            .setGroup(NOTIF_GROUP_DYNAMIC_ISLAND).build()
        notification.extras.putString(EXTRA_MIUI_FOCUS_PARAM, hyperIslandBuilder.buildJsonParam())

        notificationManager.notify(notifId, notification)
    }

    private fun pendingActivityIntent(): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(Intent.ACTION_MAIN).apply {
                setClassName(context.packageName, "${context.packageName}.MainActivity")
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            NOTIF_REQUEST_CODE_OFFSET,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dynamicIslandNotificationId(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + 4000 + sessionId.hashCode()
}
