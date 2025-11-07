package com.fakhry.pomodojo.preferences.domain.model

sealed class TimelineSegmentDomain(open val duration: Int) {
    data class Focus(override val duration: Int) : TimelineSegmentDomain(duration)
    data class ShortBreak(override val duration: Int) : TimelineSegmentDomain(duration)
    data class LongBreak(override val duration: Int) : TimelineSegmentDomain(duration)
}