package com.fakhry.pomodojo.core.designsystem.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TimelineUiModel(
    val segments: ImmutableList<TimelineSegmentUi> = persistentListOf(),
    val hourSplits: ImmutableList<Int> = persistentListOf(),
)

@Immutable
data class PreferenceOption<T>(
    val label: String,
    val value: T,
    val selected: Boolean,
    val enabled: Boolean = true,
)

/**
 * Represents a single segment in the Pomodoro timeline.
 * Each segment has a specific type and duration.
 */
@Immutable
data class TimelineSegmentUi(
    val type: TimerTypeUi = TimerTypeUi.FOCUS,
    val cycleNumber: Int = 0,
    val timer: TimerUi = TimerUi(),
    val timerStatus: TimerStatusUi = TimerStatusUi.INITIAL,
)

@Immutable
data class TimerUi(
    val progress: Float = 0f,
    val durationEpochMs: Long = 0L,
    val finishedInMillis: Long = 0L,
    val formattedTime: String = "00:00",
    val startedPauseTime: Long = 0L,
    val elapsedPauseTime: Long = 0L,
)
