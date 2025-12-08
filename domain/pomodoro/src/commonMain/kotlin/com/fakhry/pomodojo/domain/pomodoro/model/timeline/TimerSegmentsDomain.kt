package com.fakhry.pomodojo.domain.pomodoro.model.timeline

enum class TimerType {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

enum class TimerStatusDomain {
    INITIAL,
    COMPLETED,
    RUNNING,
    PAUSED,
}

data class TimerSegmentsDomain(
    val type: TimerType = TimerType.FOCUS,
    val cycleNumber: Int = 0,
    val timer: TimerDomain = TimerDomain(),
    val timerStatus: TimerStatusDomain = TimerStatusDomain.INITIAL,
)

data class TimerDomain(
    val progress: Float = 0f,
    val durationEpochMs: Long = 0L,
    val finishedInMillis: Long = 0L,
    val startedPauseTime: Long = 0L,
    val elapsedPauseTime: Long = 0L,
)

data class TimelineDomain(
    val segments: List<TimerSegmentsDomain> = emptyList(),
    val hourSplits: List<Int> = emptyList(),
)
