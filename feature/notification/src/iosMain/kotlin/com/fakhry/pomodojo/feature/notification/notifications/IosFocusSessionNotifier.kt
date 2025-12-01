package com.fakhry.pomodojo.feature.notification.notifications

import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.features.focus.ui.mapper.toCompletionSummary
import com.fakhry.pomodojo.focus.domain.mapper.toIosNotificationSummary
import com.fakhry.pomodojo.shared.domain.model.focus.CompletionNotificationSummary
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Clock

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier = IosFocusSessionNotifier()

private const val COMPLETION_NOTIFICATION_ID = "focus_session_completion_notification"
private const val SEGMENT_COMPLETION_NOTIFICATION_ID = "focus_segment_completion_notification"

@OptIn(ExperimentalForeignApi::class, kotlin.time.ExperimentalTime::class)
class IosFocusSessionNotifier : PomodoroSessionNotifier {
    private var activeLiveActivitySessionId: String? = null
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun schedule(snapshot: PomodoroSessionDomain) {
        val isLiveActivitySupported = LiveActivityManager.isSupported()
        if (!isLiveActivitySupported) {
            println("IosFocusSessionNotifier: LiveActivities not supported, fallback to notifs")
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val summary = snapshot.toIosNotificationSummary(now)

        println("IosFocusSessionNotifier: Updating Live Activity for session ${summary.sessionId}")

        // Check if all segments are completed
        if (summary.isAllSegmentsCompleted) {
            val completionSummary = snapshot.toCompletionSummary()
            if (isLiveActivitySupported) {
                LiveActivityManager.endLiveActivityWithCompletion(
                    completedCycles = completionSummary.completedCycles,
                    totalFocusMinutes = completionSummary.totalFocusMinutes,
                    totalBreakMinutes = completionSummary.totalBreakMinutes,
                )
            }
            activeLiveActivitySessionId = null
            scheduleCompletionNotification(completionSummary)
            return
        }

        // Get current segment details
        val currentSegment = snapshot.timeline.segments.firstOrNull {
            it.timerStatus == TimerStatusDomain.RUNNING ||
                it.timerStatus == TimerStatusDomain.PAUSED
        } ?: snapshot.timeline.segments.firstOrNull {
            it.timerStatus != TimerStatusDomain.COMPLETED
        }

        if (currentSegment == null) {
            println("IosFocusSessionNotifier: No active segment found")
            return
        }

        val remaining = calculateRemainingSeconds(currentSegment, now)
        val totalSeconds = (currentSegment.timer.durationEpochMs / 1000).toInt()
        val segmentType = currentSegment.type.toSegmentTypeString()
        val isPaused = currentSegment.timerStatus == TimerStatusDomain.PAUSED
        val schedulePayload = snapshot.buildLiveActivitySchedulePayload(now)
        val scheduleJson = schedulePayload?.toJsonString()
        val sessionId = summary.sessionId
        if (!isPaused && schedulePayload != null) {
            scheduleSegmentCompletionChimes(sessionId, schedulePayload)
        } else {
            cancelSegmentCompletionChime(sessionId)
        }

        // Start or update Live Activity
        if (!isLiveActivitySupported) return

        try {
            // For the first segment of the session, start the Live Activity
            // Otherwise, update the existing Live Activity
            val isFirstSegment = snapshot.timeline.segments.indexOfFirst {
                it.timerStatus == TimerStatusDomain.RUNNING ||
                    it.timerStatus == TimerStatusDomain.PAUSED
            } == 0
            val hasNoCompletedSegments = snapshot.timeline.segments.none {
                it.timerStatus == TimerStatusDomain.COMPLETED
            }

            val hasStartedForSession = activeLiveActivitySessionId == sessionId

            if (!hasStartedForSession && isFirstSegment && hasNoCompletedSegments) {
                println("IosFocusSessionNotifier: Starting Live Activity")
                LiveActivityManager.startLiveActivity(
                    sessionId = sessionId,
                    quote = summary.quote,
                    cycleNumber = currentSegment.cycleNumber,
                    totalCycles = snapshot.totalCycle,
                    segmentType = segmentType,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds,
                    isPaused = isPaused,
                    scheduleJson = scheduleJson,
                )
                activeLiveActivitySessionId = sessionId
            } else {
                println("IosFocusSessionNotifier: Updating Live Activity")
                LiveActivityManager.updateLiveActivity(
                    cycleNumber = currentSegment.cycleNumber,
                    totalCycles = snapshot.totalCycle,
                    segmentType = segmentType,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds,
                    isPaused = isPaused,
                    scheduleJson = scheduleJson,
                )
            }
        } catch (e: Exception) {
            println("IosFocusSessionNotifier: Failed to update Live Activity: ${e.message}")
        }
    }

    override suspend fun cancel(sessionId: String) {
        println("IosFocusSessionNotifier: Ending Live Activity for session $sessionId")
        LiveActivityManager.endLiveActivity()
        activeLiveActivitySessionId = null
        cancelSegmentCompletionChime(sessionId)

        // Also cancel any pending completion notifications
        val identifiers = listOf(COMPLETION_NOTIFICATION_ID + "_" + sessionId)
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    private fun calculateRemainingSeconds(segment: TimerSegmentsDomain, now: Long): Int {
        val remainingMillis = when (segment.timerStatus) {
            TimerStatusDomain.COMPLETED -> 0L
            TimerStatusDomain.INITIAL -> segment.timer.durationEpochMs
            TimerStatusDomain.RUNNING -> (segment.timer.finishedInMillis - now).coerceAtLeast(0L)
            TimerStatusDomain.PAUSED -> {
                val remaining = segment.timer.finishedInMillis - segment.timer.startedPauseTime
                remaining.coerceAtLeast(0L)
            }
        }
        return (remainingMillis / 1000).toInt()
    }

    private suspend fun scheduleCompletionNotification(summary: CompletionNotificationSummary) {
        val title = "Amazing work!"
        val body = "You crushed ${summary.completedCycles} cycles with " +
            "${summary.totalFocusMinutes} minutes of focused work and " +
            "${summary.totalBreakMinutes} minutes of well-deserved breaks. Keep up the momentum!"

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setUserInfo(
                mapOf(
                    "sessionId" to summary.sessionId,
                    "type" to "session_completion",
                ) as Map<Any?, *>,
            )
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 1.0,
            repeats = false,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = COMPLETION_NOTIFICATION_ID + "_" + summary.sessionId,
            content = content,
            trigger = trigger,
        )

        notificationCenter.addNotificationRequest(request) { error ->
            error?.let {
                println(
                    "IosFocusSessionNotifier: Failed to schedule completion notification: ${it.localizedDescription}",
                )
            }
        }
        cancelSegmentCompletionChime(summary.sessionId)
    }

    private suspend fun scheduleSegmentCompletionChimes(
        sessionId: String,
        schedule: LiveActivitySchedulePayload,
    ) {
        cancelSegmentCompletionChime(sessionId)

        val prefix = "${SEGMENT_COMPLETION_NOTIFICATION_ID}_$sessionId"
        schedule.segments.forEachIndexed { index, entry ->
            val completionDelay = entry.startOffsetSeconds + entry.totalSeconds
            if (completionDelay <= 0) return@forEachIndexed

            val identifier = "${prefix}_$index"
            val nextLabel = schedule
                .segments
                .getOrNull(index + 1)
                ?.type
                ?.toReadableSegmentLabel()

            scheduleSegmentCompletionChime(
                identifier = identifier,
                sessionId = sessionId,
                fireInSeconds = completionDelay,
                nextLabel = nextLabel,
            )
        }
    }

    private fun scheduleSegmentCompletionChime(
        identifier: String,
        sessionId: String,
        fireInSeconds: Int,
        nextLabel: String?,
    ) {
        if (fireInSeconds <= 0) return

        val content = UNMutableNotificationContent().apply {
            setTitle("Segment complete")
            val body = nextLabel?.let { "Next: $it" } ?: "Time to switch modes."
            setBody(body)
            setSound(
                UNNotificationSound.soundNamed(
                    "timer_notification.wav",
                ),
            )
            setUserInfo(mapOf("sessionId" to sessionId) as Map<Any?, *>)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = fireInSeconds.toDouble(),
            repeats = false,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
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

    private fun String.toReadableSegmentLabel(): String? = when (this) {
        "focus" -> "Focus"
        "short_break" -> "Break"
        "long_break" -> "Long break"
        else -> null
    }
}
