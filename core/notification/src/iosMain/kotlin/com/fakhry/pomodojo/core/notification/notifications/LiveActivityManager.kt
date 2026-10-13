package com.fakhry.pomodojo.core.notification.notifications

import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.liveactivity.bridge.PomodoroLiveActivityBridge
import kotlinx.cinterop.ExperimentalForeignApi

private const val MILLIS_PER_SECOND = 1000L

/**
 * LiveActivityManager - Kotlin wrapper for PomodoroLiveActivityBridge
 *
 * Calls the Objective-C bridge which then calls Swift Live Activity code.
 * Uses cinterop-generated bindings to call Objective-C methods.
 */
@OptIn(ExperimentalForeignApi::class)
object LiveActivityManager {

    fun isSupported(): Boolean = try {
        val supported = PomodoroLiveActivityBridge.isSupported()
        println("LiveActivityManager: isSupported = $supported")
        supported
    } catch (e: Exception) {
        println("LiveActivityManager: isSupported failed: ${e.message}")
        false
    }

    fun startLiveActivity(notice: SessionNotice.Showing) {
        try {
            println("LiveActivityManager: Starting Live Activity for ${notice.sessionId}")
            PomodoroLiveActivityBridge.shared().startLiveActivityWithSessionId(
                sessionId = notice.sessionId,
                quote = notice.quote.withAttribution(),
                cycleNumber = notice.segment.cycleNumber.toLong(),
                totalCycles = notice.totalCycle.toLong(),
                segmentType = notice.segment.type.toSegmentTypeString(),
                remainingSeconds = notice.remainingMs / MILLIS_PER_SECOND,
                totalSeconds = notice.segment.durationMs / MILLIS_PER_SECOND,
                endsAtEpochMs = notice.endsAtEpochMs(),
                isPaused = notice !is SessionNotice.Running,
                nextSegmentType = notice.nextSegmentType(),
            )
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to start: ${e.message}")
            e.printStackTrace()
        }
    }

    fun updateLiveActivity(notice: SessionNotice.Showing) {
        try {
            println("LiveActivityManager: Updating Live Activity for ${notice.sessionId}")
            PomodoroLiveActivityBridge.shared().updateLiveActivityWithCycleNumber(
                cycleNumber = notice.segment.cycleNumber.toLong(),
                totalCycles = notice.totalCycle.toLong(),
                segmentType = notice.segment.type.toSegmentTypeString(),
                remainingSeconds = notice.remainingMs / MILLIS_PER_SECOND,
                totalSeconds = notice.segment.durationMs / MILLIS_PER_SECOND,
                endsAtEpochMs = notice.endsAtEpochMs(),
                isPaused = notice !is SessionNotice.Running,
                nextSegmentType = notice.nextSegmentType(),
            )
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to update: ${e.message}")
        }
    }

    fun endLiveActivity() {
        try {
            println("LiveActivityManager: Ending Live Activity")

            val bridge = PomodoroLiveActivityBridge.shared()
            bridge.endLiveActivity()
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to end: ${e.message}")
        }
    }
}

/** 0 when nothing counts down. */
private fun SessionNotice.Showing.endsAtEpochMs(): Long =
    (this as? SessionNotice.Running)?.endsAtMs ?: 0L

/** The Segment waiting at the Gate, or null when not at the Gate. */
private fun SessionNotice.Showing.nextSegmentType(): String? =
    (this as? SessionNotice.AtGate)?.next?.type?.toSegmentTypeString()

private fun TimerType.toSegmentTypeString(): String = when (this) {
    TimerType.FOCUS -> "focus"
    TimerType.SHORT_BREAK -> "short_break"
    TimerType.LONG_BREAK -> "long_break"
}
