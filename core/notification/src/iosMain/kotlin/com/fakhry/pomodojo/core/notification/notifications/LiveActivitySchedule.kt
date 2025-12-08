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
    val segmentsToSchedule = timeline.segments
        .dropWhile { it.timerStatus == TimerStatusDomain.COMPLETED }
    if (segmentsToSchedule.isEmpty()) return null

    val entries = mutableListOf<LiveActivitySegmentEntry>()
    var offsetSeconds = 0
    segmentsToSchedule.forEachIndexed { index, segment ->
        val totalSeconds = (segment.timer.durationEpochMs / 1000).toInt().coerceAtLeast(1)
        val elapsedSeconds = segment.elapsedSeconds(nowMillis, index == 0)
        val startOffset = if (index == 0) -elapsedSeconds else offsetSeconds

        entries += LiveActivitySegmentEntry(
            type = segment.type.toSegmentTypeString(),
            cycleNumber = segment.cycleNumber,
            totalSeconds = totalSeconds,
            startOffsetSeconds = startOffset,
        )

        offsetSeconds = if (index == 0) {
            segment.remainingSeconds(nowMillis)
        } else {
            offsetSeconds + totalSeconds
        }
    }

    return LiveActivitySchedulePayload(
        generatedAtEpochMillis = nowMillis,
        segments = entries,
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

private fun TimerSegmentsDomain.remainingSeconds(nowMillis: Long): Int = when (timerStatus) {
    TimerStatusDomain.COMPLETED -> 0
    TimerStatusDomain.INITIAL -> (timer.durationEpochMs / 1000).toInt()
    TimerStatusDomain.RUNNING -> (
        (timer.finishedInMillis - nowMillis).coerceAtLeast(
            0L,
        ) / 1000
        ).toInt()

    TimerStatusDomain.PAUSED -> {
        val remaining = timer.finishedInMillis - timer.startedPauseTime
        (remaining.coerceAtLeast(0L) / 1000).toInt()
    }
}

internal fun TimerType.toSegmentTypeString(): String = when (this) {
    TimerType.FOCUS -> "focus"
    TimerType.SHORT_BREAK -> "short_break"
    TimerType.LONG_BREAK -> "long_break"
}
