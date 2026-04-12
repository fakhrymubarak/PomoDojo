package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import com.fakhry.pomodojo.core.notification.R as RP

private const val PIC_KEY_ICON = "pomodojo_icon"
private const val COLOR_PAUSE_RESUME_BG = "#E0E0E0"
private const val COLOR_STOP_BG = "#FF3B30"
private const val COLOR_STOP_TEXT = "#FFFFFF"

private const val MILLIS_IN_MINUTE = 60_000L
private const val MILLIS_IN_SECOND = 1_000L
private const val TIME_ZERO = "00:00"

private const val ISLAND_CHANNEL_ID = "focus"
private const val ISLAND_CHANNEL_NAME = "Focus"

private const val ACTION_KEY_PAUSE_RESUME = "pause_resume"
private const val ACTION_KEY_STOP = "stop"

private const val ACTION_TYPE_CLICK = 1
private const val INFO_TYPE_IMAGE_TEXT = 1
private const val PIC_TYPE_NORMAL = 1

private const val NOTIF_GROUP_DYNAMIC_ISLAND = "dynamic_island"
private const val EXTRA_MIUI_FOCUS_PARAM = "miui.focus.param"

// TODO:
// 1. Define data class to show timer in segments.
/**
 * Xiaomi Dynamic Island Notification Implementation
 *
 * */
class XiaomiNotifManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) : NotifManager {

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
        val picKey = PIC_KEY_ICON
        val pic = HyperPicture(picKey, context, RP.drawable.ic_timer)

        val pauseResumeLabel = if (summary.isPaused) "Resume" else "Pause"
        val actPause = HyperAction(
            key = ACTION_KEY_PAUSE_RESUME,
            title = pauseResumeLabel,
            icon = null,
            pendingIntent = intent,
            actionIntentType = ACTION_TYPE_CLICK,
            actionBgColor = COLOR_PAUSE_RESUME_BG
        )
        val actStop = HyperAction(
            key = ACTION_KEY_STOP,
            title = "Stop",
            icon = null,
            pendingIntent = intent,
            actionIntentType = ACTION_TYPE_CLICK,
            actionBgColor = COLOR_STOP_BG,
            titleColor = COLOR_STOP_TEXT
        )

        val remainingMillis =
            (summary.finishTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val formattedTime = if (remainingMillis > 0) {
            val minutes = remainingMillis / MILLIS_IN_MINUTE
            val seconds = (remainingMillis % MILLIS_IN_MINUTE) / MILLIS_IN_SECOND
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            TIME_ZERO
        }


        val countdownTimer = TimerInfo(
            -1,
            summary.finishTimeMillis,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )

        // Show Timer IslanNotification
        val hyperIslandBuilder =
            HyperIslandNotification.Builder(context, ISLAND_CHANNEL_ID, ISLAND_CHANNEL_NAME)
                .setSmallWindowTarget("${context.packageName}.MainActivity")
                .setChatInfo(title = summary.title, timer = countdownTimer, pictureKey = picKey)
                .setBigIslandCountdown(summary.finishTimeMillis, picKey)

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
