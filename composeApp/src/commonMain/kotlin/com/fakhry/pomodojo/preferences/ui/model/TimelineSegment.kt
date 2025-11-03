package com.fakhry.pomodojo.preferences.ui.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class TimelineSegment(open val durationMinutes: Int) {
    @Immutable data class Focus(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
    @Immutable data class ShortBreak(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
    @Immutable data class LongBreak(override val durationMinutes: Int) : TimelineSegment(durationMinutes)
}