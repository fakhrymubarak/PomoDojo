package com.fakhry.pomodojo.focus.domain

import com.fakhry.pomodojo.focus.domain.mapper.toIosNotificationSummary
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.ui.mapper.toCompletionSummary
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock

actual fun provideFocusSessionNotifier(): FocusSessionNotifier = IosFocusSessionNotifier()

private const val COMPLETION_NOTIFICATION_ID = "focus_session_completion_notification"

@OptIn(ExperimentalForeignApi::class, kotlin.time.ExperimentalTime::class)
class IosFocusSessionNotifier : FocusSessionNotifier {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun schedule(snapshot: PomodoroSessionDomain) {
        val now = Clock.System.now().toEpochMilliseconds()
        val summary = snapshot.toIosNotificationSummary(now)

        println("IosFocusSessionNotifier: Updating Live Activity for session ${summary.sessionId}")

        // Check if all segments are completed
        if (summary.isAllSegmentsCompleted) {
            val completionSummary = snapshot.toCompletionSummary()
            LiveActivityManager.endLiveActivityWithCompletion(
                completedCycles = completionSummary.completedCycles,
                totalFocusMinutes = completionSummary.totalFocusMinutes,
                totalBreakMinutes = completionSummary.totalBreakMinutes,
            )
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

        // Check if Live Activity is already running
        if (!LiveActivityManager.isSupported()) {
            println("IosFocusSessionNotifier: Live Activities not supported")
            return
        }

        // Start or update Live Activity
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

            if (isFirstSegment && hasNoCompletedSegments) {
                println("IosFocusSessionNotifier: Starting Live Activity")
                LiveActivityManager.startLiveActivity(
                    sessionId = summary.sessionId,
                    quote = summary.quote,
                    cycleNumber = currentSegment.cycleNumber,
                    totalCycles = snapshot.totalCycle,
                    segmentType = segmentType,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds,
                    isPaused = isPaused,
                )
            } else {
                println("IosFocusSessionNotifier: Updating Live Activity")
                LiveActivityManager.updateLiveActivity(
                    cycleNumber = currentSegment.cycleNumber,
                    totalCycles = snapshot.totalCycle,
                    segmentType = segmentType,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds,
                    isPaused = isPaused,
                )
            }
        } catch (e: Exception) {
            println("IosFocusSessionNotifier: Failed to update Live Activity: ${e.message}")
        }
    }

    override suspend fun cancel(sessionId: String) {
        println("IosFocusSessionNotifier: Ending Live Activity for session $sessionId")
        LiveActivityManager.endLiveActivity()

        // Also cancel any pending completion notifications
        val identifiers = listOf(COMPLETION_NOTIFICATION_ID + "_" + sessionId)
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    private fun calculateRemainingSeconds(
        segment: com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain,
        now: Long,
    ): Int {
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

    private fun TimerType.toSegmentTypeString(): String = when (this) {
        TimerType.FOCUS -> "focus"
        TimerType.SHORT_BREAK -> "short_break"
        TimerType.LONG_BREAK -> "long_break"
    }

    private fun scheduleCompletionNotification(
        summary: com.fakhry.pomodojo.focus.domain.model.CompletionNotificationSummary,
    ) {
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
            timeInterval = 0.1,
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
    }
}
