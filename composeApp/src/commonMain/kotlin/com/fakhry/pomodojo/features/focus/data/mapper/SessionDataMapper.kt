package com.fakhry.pomodojo.features.focus.data.mapper

import com.fakhry.pomodojo.core.database.entities.HistorySessionEntity
import com.fakhry.pomodojo.core.utils.primitives.toMinutes
import com.fakhry.pomodojo.features.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.features.focus.data.model.PomodoroSessionData
import com.fakhry.pomodojo.features.focus.data.model.QuoteContentData
import com.fakhry.pomodojo.features.focus.data.model.TimelineData
import com.fakhry.pomodojo.features.focus.data.model.TimerData
import com.fakhry.pomodojo.features.focus.data.model.TimerSegmentData
import com.fakhry.pomodojo.features.focus.data.model.TimerStatusData
import com.fakhry.pomodojo.features.focus.data.model.TimerTypeData
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.quote.QuoteContent
import com.fakhry.pomodojo.shared.domain.model.timeline.TimelineDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun PomodoroSessionData.toDomain(): PomodoroSessionDomain = PomodoroSessionDomain(
    totalCycle = totalCycle,
    startedAtEpochMs = startedAtEpochMs,
    elapsedPauseEpochMs = elapsedPauseEpochMs,
    timeline = timeline.toDomain(),
    quote = quote.toDomain(),
)

fun PomodoroSessionDomain.toData(): PomodoroSessionData = PomodoroSessionData(
    totalCycle = totalCycle,
    startedAtEpochMs = startedAtEpochMs,
    elapsedPauseEpochMs = elapsedPauseEpochMs,
    timeline = timeline.toData(),
    quote = quote.toData(),
)

private fun TimelineData.toDomain(): TimelineDomain = TimelineDomain(
    segments = segments.map { it.toDomain() },
    hourSplits = hourSplits,
)

private fun TimelineDomain.toData(): TimelineData = TimelineData(
    segments = segments.map { it.toData() },
    hourSplits = hourSplits,
)

private fun TimerSegmentData.toDomain(): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type.toDomain(),
    cycleNumber = cycleNumber,
    timer = timer.toDomain(),
    timerStatus = timerStatus.toDomain(),
)

private fun TimerSegmentsDomain.toData(): TimerSegmentData = TimerSegmentData(
    type = type.toData(),
    cycleNumber = cycleNumber,
    timer = timer.toData(),
    timerStatus = timerStatus.toData(),
)

private fun TimerData.toDomain(): TimerDomain = TimerDomain(
    progress = progress,
    durationEpochMs = durationEpochMs,
    finishedInMillis = finishedInMillis,
    startedPauseTime = startedPauseTime,
    elapsedPauseTime = elapsedPauseTime,
)

private fun TimerDomain.toData(): TimerData = TimerData(
    progress = progress,
    durationEpochMs = durationEpochMs,
    finishedInMillis = finishedInMillis,
    startedPauseTime = startedPauseTime,
    elapsedPauseTime = elapsedPauseTime,
)

private fun QuoteContentData.toDomain(): QuoteContent = QuoteContent(
    id = id,
    text = text,
    character = character,
    sourceTitle = sourceTitle,
    metadata = metadata,
)

private fun QuoteContent.toData(): QuoteContentData = QuoteContentData(
    id = id,
    text = text,
    character = character,
    sourceTitle = sourceTitle,
    metadata = metadata,
)

private fun TimerTypeData.toDomain(): TimerType = when (this) {
    TimerTypeData.FOCUS -> TimerType.FOCUS
    TimerTypeData.SHORT_BREAK -> TimerType.SHORT_BREAK
    TimerTypeData.LONG_BREAK -> TimerType.LONG_BREAK
}

private fun TimerType.toData(): TimerTypeData = when (this) {
    TimerType.FOCUS -> TimerTypeData.FOCUS
    TimerType.SHORT_BREAK -> TimerTypeData.SHORT_BREAK
    TimerType.LONG_BREAK -> TimerTypeData.LONG_BREAK
}

private fun TimerStatusData.toDomain(): TimerStatusDomain = when (this) {
    TimerStatusData.INITIAL -> TimerStatusDomain.INITIAL
    TimerStatusData.COMPLETED -> TimerStatusDomain.COMPLETED
    TimerStatusData.RUNNING -> TimerStatusDomain.RUNNING
    TimerStatusData.PAUSED -> TimerStatusDomain.PAUSED
}

private fun TimerStatusDomain.toData(): TimerStatusData = when (this) {
    TimerStatusDomain.INITIAL -> TimerStatusData.INITIAL
    TimerStatusDomain.COMPLETED -> TimerStatusData.COMPLETED
    TimerStatusDomain.RUNNING -> TimerStatusData.RUNNING
    TimerStatusDomain.PAUSED -> TimerStatusData.PAUSED
}

fun PomodoroSessionDomain.toHistoryEntity(): HistorySessionEntity {
    val elapsedSegments = timeline.segments.filterNot {
        it.timerStatus == TimerStatusDomain.INITIAL
    }
    val totalFocusMinutes = elapsedSegments
        .filter { it.type == TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()
    val totalBreakMinutes = elapsedSegments
        .filter { it.type != TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()

    return HistorySessionEntity(
        id = startedAtEpochMs,
        dateStartedEpochMs = startedAtEpochMs,
        totalFocusMinutes = totalFocusMinutes,
        totalBreakMinutes = totalBreakMinutes,
    )
}

fun List<HistorySessionEntity>.mapToDomain(timeZone: TimeZone): List<HistoryDomain> =
    map { it.toDomain(timeZone) }

@OptIn(ExperimentalTime::class)
private fun HistorySessionEntity.toDomain(timeZone: TimeZone): HistoryDomain {
    val date =
        Instant.fromEpochMilliseconds(dateStartedEpochMs).toLocalDateTime(timeZone).date.toString()
    return HistoryDomain(
        date = date,
        focusMinutes = totalFocusMinutes,
        breakMinutes = totalBreakMinutes,
    )
}
