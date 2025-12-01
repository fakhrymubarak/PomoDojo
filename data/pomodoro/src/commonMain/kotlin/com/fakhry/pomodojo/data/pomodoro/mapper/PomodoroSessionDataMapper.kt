package com.fakhry.pomodojo.data.pomodoro.mapper

import androidx.datastore.preferences.core.Preferences
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.core.datastore.model.PomodoroSessionDataStore
import com.fakhry.pomodojo.core.datastore.model.QuoteContentData
import com.fakhry.pomodojo.core.datastore.model.TimelineData
import com.fakhry.pomodojo.core.datastore.model.TimerData
import com.fakhry.pomodojo.core.datastore.model.TimerSegmentData
import com.fakhry.pomodojo.core.datastore.model.TimerStatusData
import com.fakhry.pomodojo.core.datastore.model.TimerTypeData
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import kotlinx.serialization.json.Json

fun Preferences.toPomodoroSession(json: Json): PomodoroSessionDomain {
    val snapshot = this[PreferenceKeys.ACTIVE_SESSION_KEY] ?: return PomodoroSessionDomain()
    return json.decodeFromString<PomodoroSessionDataStore>(snapshot).toDomain()
}

fun PomodoroSessionDomain.toDataStore(): PomodoroSessionDataStore = PomodoroSessionDataStore(
    totalCycle = totalCycle,
    startedAtEpochMs = startedAtEpochMs,
    elapsedPauseEpochMs = elapsedPauseEpochMs,
    timeline = timeline.toData(),
    quote = quote.toData(),
)

private fun PomodoroSessionDataStore.toDomain(): PomodoroSessionDomain = PomodoroSessionDomain(
    totalCycle = totalCycle,
    startedAtEpochMs = startedAtEpochMs,
    elapsedPauseEpochMs = elapsedPauseEpochMs,
    timeline = timeline.toDomain(),
    quote = quote.toDomain(),
)

private fun QuoteContentData.toDomain(): QuoteContent = QuoteContent(
    id = id,
    text = text,
    character = character,
    sourceTitle = sourceTitle,
    metadata = metadata,
)

private fun TimelineData.toDomain(): TimelineDomain = TimelineDomain(
    segments = segments.map { it.toDomain() },
    hourSplits = hourSplits,
)

private fun TimerSegmentData.toDomain(): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type.toDomain(),
    cycleNumber = cycleNumber,
    timer = timer.toDomain(),
    timerStatus = timerStatus.toDomain(),
)

private fun TimerTypeData.toDomain(): TimerType = when (this) {
    TimerTypeData.FOCUS -> TimerType.FOCUS
    TimerTypeData.SHORT_BREAK -> TimerType.SHORT_BREAK
    TimerTypeData.LONG_BREAK -> TimerType.LONG_BREAK
}

private fun TimerData.toDomain(): TimerDomain = TimerDomain(
    progress = progress,
    durationEpochMs = durationEpochMs,
    finishedInMillis = finishedInMillis,
    startedPauseTime = startedPauseTime,
    elapsedPauseTime = elapsedPauseTime,
)

private fun TimerStatusData.toDomain(): TimerStatusDomain = when (this) {
    TimerStatusData.INITIAL -> TimerStatusDomain.INITIAL
    TimerStatusData.COMPLETED -> TimerStatusDomain.COMPLETED
    TimerStatusData.RUNNING -> TimerStatusDomain.RUNNING
    TimerStatusData.PAUSED -> TimerStatusDomain.PAUSED
}


private fun TimelineDomain.toData(): TimelineData = TimelineData(
    segments = segments.map { it.toData() },
    hourSplits = hourSplits,
)

private fun TimerSegmentsDomain.toData(): TimerSegmentData = TimerSegmentData(
    type = type.toData(),
    cycleNumber = cycleNumber,
    timer = timer.toData(),
    timerStatus = timerStatus.toData(),
)

private fun TimerDomain.toData(): TimerData = TimerData(
    progress = progress,
    durationEpochMs = durationEpochMs,
    finishedInMillis = finishedInMillis,
    startedPauseTime = startedPauseTime,
    elapsedPauseTime = elapsedPauseTime,
)

private fun QuoteContent.toData(): QuoteContentData = QuoteContentData(
    id = id,
    text = text,
    character = character,
    sourceTitle = sourceTitle,
    metadata = metadata,
)

private fun TimerType.toData(): TimerTypeData = when (this) {
    TimerType.FOCUS -> TimerTypeData.FOCUS
    TimerType.SHORT_BREAK -> TimerTypeData.SHORT_BREAK
    TimerType.LONG_BREAK -> TimerTypeData.LONG_BREAK
}

private fun TimerStatusDomain.toData(): TimerStatusData = when (this) {
    TimerStatusDomain.INITIAL -> TimerStatusData.INITIAL
    TimerStatusDomain.COMPLETED -> TimerStatusData.COMPLETED
    TimerStatusDomain.RUNNING -> TimerStatusData.RUNNING
    TimerStatusDomain.PAUSED -> TimerStatusData.PAUSED
}
