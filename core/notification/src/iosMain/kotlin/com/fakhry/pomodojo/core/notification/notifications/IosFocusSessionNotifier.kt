package com.fakhry.pomodojo.core.notification.notifications

import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier = IosFocusSessionNotifier()

private const val SEGMENT_COMPLETION_NOTIFICATION_ID = "focus_segment_completion_notification"
private const val MILLIS_PER_SECOND = 1000.0

class IosFocusSessionNotifier : PomodoroSessionNotifier {
    private var activeLiveActivitySessionId: String? = null
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun show(notice: SessionNotice) {
        when (notice) {
            is SessionNotice.Running -> scheduleSegmentCompletionChime(notice)
            is SessionNotice.Paused -> cancelSegmentCompletionChime(notice.sessionId)
            // Keep the just-delivered "Phase complete" chime; nothing is pending at the Gate.
            is SessionNotice.AtGate -> Unit
            is SessionNotice.Cleared -> {
                cancelSegmentCompletionChime(notice.sessionId)
                LiveActivityManager.endLiveActivity()
                activeLiveActivitySessionId = null
            }
        }
        if (notice !is SessionNotice.Showing) return

        if (!LiveActivityManager.isSupported()) {
            println("IosFocusSessionNotifier: LiveActivities not supported, fallback to notifs")
            return
        }
        // The Swift side adopts a Live Activity already showing this Session (e.g. after the
        // app was relaunched) instead of starting a second one.
        if (activeLiveActivitySessionId == notice.sessionId) {
            LiveActivityManager.updateLiveActivity(notice)
        } else {
            LiveActivityManager.startLiveActivity(notice)
            activeLiveActivitySessionId = notice.sessionId
        }
    }

    private suspend fun scheduleSegmentCompletionChime(notice: SessionNotice.Running) {
        cancelSegmentCompletionChime(notice.sessionId)
        if (notice.remainingMs <= 0L) return

        val content = UNMutableNotificationContent().apply {
            setTitle("Phase complete")
            // Phase-transition gate: the next phase waits for the user to tap continue.
            setBody("Tap to continue to your next phase")
            setSound(
                UNNotificationSound.soundNamed(
                    "timer_notification.wav",
                ),
            )
            setUserInfo(mapOf("sessionId" to notice.sessionId) as Map<Any?, *>)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = notice.remainingMs / MILLIS_PER_SECOND,
            repeats = false,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "${SEGMENT_COMPLETION_NOTIFICATION_ID}_${notice.sessionId}",
            content = content,
            trigger = trigger,
        )

        notificationCenter.addNotificationRequest(request) { error ->
            error?.let {
                println(
                    "IosFocusSessionNotifier: Failed to schedule segment chime: ${it.localizedDescription}",
                )
            }
        }
    }

    private suspend fun cancelSegmentCompletionChime(sessionId: String) {
        val prefix = "${SEGMENT_COMPLETION_NOTIFICATION_ID}_$sessionId"
        val pending = fetchPendingNotificationIds(prefix)
        if (pending.isNotEmpty()) {
            notificationCenter.removePendingNotificationRequestsWithIdentifiers(pending)
        }
        val delivered = fetchDeliveredNotificationIds(prefix)
        if (delivered.isNotEmpty()) {
            notificationCenter.removeDeliveredNotificationsWithIdentifiers(delivered)
        }
    }

    private suspend fun fetchPendingNotificationIds(prefix: String): List<String> =
        suspendCancellableCoroutine { continuation ->
            notificationCenter.getPendingNotificationRequestsWithCompletionHandler { requests ->
                val identifiers = (requests as? List<*>)?.mapNotNull { request ->
                    val pendingRequest = request as? UNNotificationRequest
                    val identifier = pendingRequest?.identifier
                    if (identifier != null && identifier.startsWith(prefix)) identifier else null
                } ?: emptyList()
                continuation.resume(identifiers)
            }
        }

    private suspend fun fetchDeliveredNotificationIds(prefix: String): List<String> =
        suspendCancellableCoroutine { continuation ->
            notificationCenter.getDeliveredNotificationsWithCompletionHandler { notifications ->
                val identifiers = (notifications as? List<*>)?.mapNotNull { notification ->
                    val deliveredNotification = notification as? UNNotification
                    val identifier = deliveredNotification?.request?.identifier
                    if (identifier != null && identifier.startsWith(prefix)) identifier else null
                } ?: emptyList()
                continuation.resume(identifiers)
            }
        }
}
