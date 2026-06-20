package com.fakhry.pomodojo.core.notification.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary

internal const val NOTIF_REQUEST_CODE_OFFSET = 42_000
internal const val NOTIF_CHANNEL_ID = "focus_session_channel"
internal const val NOTIF_CHANNEL_NAME = "Focus Sessions"
internal const val NOTIF_CHANNEL_DESC = "Focus session progress"
internal const val MAIN_ACTIVITY_CLASS_NAME = "com.fakhry.pomodojo.MainActivity"

// Distinct id offsets so notification and alarm ids derived from the same session never collide
internal const val ALARM_COMPLETED_ID_OFFSET = 1_000
internal const val ALARM_PROGRESS_ID_OFFSET = 2_000
internal const val NOTIF_COMPLETED_ID_OFFSET = 3_000
internal const val NOTIF_DYNAMIC_ISLAND_ID_OFFSET = 4_000

interface NotifManager {
    fun notify(summary: NotificationSummary)

    fun cancel(sessionId: String)
}

/**
 * Builds a [PendingIntent] that opens the app's launcher activity.
 *
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] can throw a
 * NullPointerException internally on some ROMs (e.g. Xiaomi/MIUI) when the resolved
 * launcher activity has a null name. We wrap it in [runCatching] so any throw is treated
 * like a null return and falls back to an explicit intent targeting [MAIN_ACTIVITY_CLASS_NAME].
 */
internal fun Context.launchActivityPendingIntent(): PendingIntent {
    val launchIntent = runCatching {
        packageManager.getLaunchIntentForPackage(packageName)
    }.getOrNull() ?: Intent(Intent.ACTION_MAIN).apply {
        setClassName(packageName, MAIN_ACTIVITY_CLASS_NAME)
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    return PendingIntent.getActivity(
        this,
        NOTIF_REQUEST_CODE_OFFSET,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
