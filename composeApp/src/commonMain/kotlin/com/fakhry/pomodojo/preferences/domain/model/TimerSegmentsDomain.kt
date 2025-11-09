package com.fakhry.pomodojo.preferences.domain.model

enum class TimerType {
    FOCUS, SHORT_BREAK, LONG_BREAK
}

data class TimerSegmentsDomain(
    val type: TimerType = TimerType.FOCUS,
    val cycleNumber: Int = 0,
    val timer: TimerDomain = TimerDomain(),
    val timerStatus: TimerStatusDomain = TimerStatusDomain.Initial,
)

data class TimerDomain(
    val durationEpochMs: Long = 0L,
    val finishedInMillis: Long = 0L,
)

data class TimelineDomain(
    val segments: List<TimerSegmentsDomain> = emptyList(),
    val hourSplits: List<Int> = emptyList(),
)

sealed class TimerStatusDomain {
    object Initial : TimerStatusDomain()
    object Completed : TimerStatusDomain()
    data class Running(val startedAtEpochMs: Long = 0L) : TimerStatusDomain()
    data class Paused(val elapsedPauseEpochMs: Long = 0L) : TimerStatusDomain()
}