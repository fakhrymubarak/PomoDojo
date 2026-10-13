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

fun TimerSegmentsDomain.remainingAt(now: Long): Long {
    val remaining = when (timerStatus) {
        TimerStatusDomain.INITIAL -> timer.durationEpochMs
        TimerStatusDomain.RUNNING -> timer.finishedInMillis - now
        TimerStatusDomain.PAUSED -> timer.finishedInMillis - timer.startedPauseTime
        TimerStatusDomain.COMPLETED -> 0L
    }
    return remaining.coerceIn(0L, timer.durationEpochMs.coerceAtLeast(0L))
}

/** Fraction of the Segment that has run; a completed Segment reports the credit it was given. */
fun TimerSegmentsDomain.progressAt(now: Long): Float = when {
    timerStatus == TimerStatusDomain.COMPLETED -> timer.progress
    timerStatus == TimerStatusDomain.INITIAL -> 0f
    timer.durationEpochMs <= 0L -> 1f
    else -> (timer.durationEpochMs - remainingAt(now)).toFloat() / timer.durationEpochMs
}
