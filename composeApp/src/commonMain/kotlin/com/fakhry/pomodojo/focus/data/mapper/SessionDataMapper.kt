package com.fakhry.pomodojo.focus.data.mapper

import com.fakhry.pomodojo.core.utils.primitives.toMinutes
import com.fakhry.pomodojo.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionHourSplitEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionSegmentEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionWithRelations
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun ActiveSessionWithRelations.toDomain(): PomodoroSessionDomain = PomodoroSessionDomain(
    totalCycle = session.totalCycle,
    startedAtEpochMs = session.startedAtEpochMs,
    elapsedPauseEpochMs = session.elapsedPausedEpochMs,
    timeline = TimelineDomain(
        segments = segments.sortedBy { it.segmentIndex }.map { it.toDomain() },
        hourSplits = hourSplits.sortedBy { it.position }.map { it.minutes },
    ),
    quote = QuoteContent(
        id = session.quoteId,
        text = session.quoteText,
        character = session.quoteCharacter,
        sourceTitle = session.quoteSourceTitle,
        metadata = session.quoteMetadata,
    ),
)

private fun ActiveSessionSegmentEntity.toDomain(): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type,
    cycleNumber = cycleNumber,
    timer = TimerDomain(
        durationEpochMs = durationEpochMs,
        finishedInMillis = finishedInMillis,
        startedPauseTime = startedPauseTime,
        elapsedPauseTime = elapsedPauseTime,
    ),
    timerStatus = timerStatus,
)

fun PomodoroSessionDomain.toEntity(sessionIdOverride: Long?): ActiveSessionEntity =
    ActiveSessionEntity(
        sessionId = sessionIdOverride ?: 0L,
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPausedEpochMs = elapsedPauseEpochMs,
        quoteId = quote.id,
        quoteText = quote.text,
        quoteCharacter = quote.character,
        quoteSourceTitle = quote.sourceTitle,
        quoteMetadata = quote.metadata,
    )

fun PomodoroSessionDomain.toSegmentEntities(sessionId: Long): List<ActiveSessionSegmentEntity> =
    timeline.segments.mapIndexed { index, segment ->
        ActiveSessionSegmentEntity(
            sessionId = sessionId,
            segmentIndex = index,
            type = segment.type,
            cycleNumber = segment.cycleNumber,
            durationEpochMs = segment.timer.durationEpochMs,
            finishedInMillis = segment.timer.finishedInMillis,
            startedPauseTime = segment.timer.startedPauseTime,
            elapsedPauseTime = segment.timer.elapsedPauseTime,
            timerStatus = segment.timerStatus,
        )
    }

fun PomodoroSessionDomain.toHourSplitEntities(
    sessionId: Long,
): List<ActiveSessionHourSplitEntity> = timeline.hourSplits.mapIndexed { index, minutes ->
    ActiveSessionHourSplitEntity(
        sessionId = sessionId,
        position = index,
        minutes = minutes,
    )
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
