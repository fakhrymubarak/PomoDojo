package com.fakhry.pomodojo.core.notification.notifications

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val liveActivityJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class LiveActivitySegmentEntry(
    val type: String,
    val cycleNumber: Int,
    val totalSeconds: Int,
    val startOffsetSeconds: Int,
)

@Serializable
data class LiveActivitySchedulePayload(
    val generatedAtEpochMillis: Long,
    val segments: List<LiveActivitySegmentEntry>,
)

internal fun LiveActivitySchedulePayload.toJsonString(): String =
    liveActivityJson.encodeToString(this)

internal fun PomodoroSessionDomain.buildLiveActivitySchedulePayload(
    nowMillis: Long,
): LiveActivitySchedulePayload? {
    // The phase-transition gate starts each phase fresh at continue-time, so future
    // phases have no known schedule. Only the currently running/paused segment can be
    // scheduled; anything else (awaiting a gate, or complete) yields no self-advancing
    // schedule and the Live Activity simply stops at the phase boundary.
    val current = timeline.segments
        .firstOrNull { it.timerStatus != TimerStatusDomain.COMPLETED }
        ?.takeIf {
            it.timerStatus == TimerStatusDomain.RUNNING ||
                it.timerStatus == TimerStatusDomain.PAUSED
        }
        ?: return null

    val totalSeconds = (current.timer.durationEpochMs / 1000).toInt().coerceAtLeast(1)
    val elapsedSeconds = current.elapsedSeconds(nowMillis, isActiveSegment = true)

    return LiveActivitySchedulePayload(
        generatedAtEpochMillis = nowMillis,
        segments = listOf(
            LiveActivitySegmentEntry(
                type = current.type.toSegmentTypeString(),
                cycleNumber = current.cycleNumber,
                totalSeconds = totalSeconds,
                startOffsetSeconds = -elapsedSeconds,
            ),
        ),
    )
}

private fun TimerSegmentsDomain.elapsedSeconds(nowMillis: Long, isActiveSegment: Boolean): Int {
    if (!isActiveSegment) return 0
    val startedAt = timer.finishedInMillis - timer.durationEpochMs
    val reference = when (timerStatus) {
        TimerStatusDomain.RUNNING -> nowMillis
        TimerStatusDomain.PAUSED -> timer.startedPauseTime
        else -> nowMillis
    }.coerceAtLeast(startedAt)
    return ((reference - startedAt).coerceAtLeast(0L) / 1000).toInt()
}

internal fun TimerType.toSegmentTypeString(): String = when (this) {
    TimerType.FOCUS -> "focus"
    TimerType.SHORT_BREAK -> "short_break"
    TimerType.LONG_BREAK -> "long_break"
}
