package com.fakhry.pomodojo.preferences.domain.model

enum class TimerType {
    FOCUS, SHORT_BREAK, LONG_BREAK
}

data class TimelineTimerDomain(
    val type: TimerType = TimerType.FOCUS,
    val cycleNumber: Int = 0,
    val timerStatus: TimerStatusDomain = TimerStatusDomain.Initial(),
)

data class TimelineDomain(
    val segments: List<TimelineTimerDomain> = emptyList(),
    val hourSplits: List<Int> = emptyList(),
)

sealed class TimerStatusDomain(
    open val progress: Float,
    open val durationEpochMs: Long = 0L,
    open val formattedTime: String = "00:00",
    open val remainingMillis: Long = 0L,

) {
    data class Initial(
        override val progress: Float = 0f,
        override val durationEpochMs: Long = 0L,
    ) : TimerStatusDomain(progress, durationEpochMs)

    data class Completed(
        override val progress: Float = 1f,
        override val durationEpochMs: Long = 0L,
    ) : TimerStatusDomain(progress, durationEpochMs)

    data class Running(
        override val progress: Float = 0f,
        override val formattedTime: String = "00:00",
        override val remainingMillis: Long = 0L,
        override val durationEpochMs: Long = 0L,
        val startedAtEpochMs: Long = 0L,
    ) : TimerStatusDomain(progress, durationEpochMs, formattedTime, remainingMillis)

    data class Paused(
        override val progress: Float = 0f,
        override val formattedTime: String = "00:00",
        override val remainingMillis: Long = 0L,
        override val durationEpochMs: Long = 0L,
        val elapsedPauseEpochMs: Long = 0L,
    ) : TimerStatusDomain(progress, durationEpochMs, formattedTime, remainingMillis)
}