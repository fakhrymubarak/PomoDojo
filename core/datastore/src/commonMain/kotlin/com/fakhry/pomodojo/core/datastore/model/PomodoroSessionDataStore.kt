package com.fakhry.pomodojo.core.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSessionDataStore(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineData = TimelineData(),
    val quote: QuoteContentData = QuoteContentData(),
)

@Serializable
data class TimelineData(
    val segments: List<TimerSegmentData> = emptyList(),
    val hourSplits: List<Int> = emptyList(),
)

@Serializable
data class TimerSegmentData(
    val type: TimerTypeData = TimerTypeData.FOCUS,
    val cycleNumber: Int = 0,
    val timer: TimerData = TimerData(),
    val timerStatus: TimerStatusData = TimerStatusData.INITIAL,
)

@Serializable
data class TimerData(
    val progress: Float = 0f,
    val durationEpochMs: Long = 0L,
    val finishedInMillis: Long = 0L,
    val startedPauseTime: Long = 0L,
    val elapsedPauseTime: Long = 0L,
)

@Serializable
data class QuoteContentData(
    val id: String = "",
    val text: String = "",
    val character: String? = null,
    val sourceTitle: String? = null,
    val metadata: String? = null,
)

@Serializable
enum class TimerTypeData {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

@Serializable
enum class TimerStatusData {
    INITIAL,
    COMPLETED,
    RUNNING,
    PAUSED,
}
