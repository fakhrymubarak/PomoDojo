package com.fakhry.pomodojo.preferences

sealed class TimelineSegment(open val durationMinutes: Int) {
    data class Focus(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
    data class ShortBreak(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
    data class LongBreak(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
}
