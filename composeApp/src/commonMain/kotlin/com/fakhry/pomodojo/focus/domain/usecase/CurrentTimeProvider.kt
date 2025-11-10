package com.fakhry.pomodojo.focus.domain.usecase

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface CurrentTimeProvider {
    fun now(): Long
    @OptIn(ExperimentalTime::class)
    fun nowInstant(): Instant
}

@OptIn(ExperimentalTime::class)
object SystemCurrentTimeProvider : CurrentTimeProvider {
    override fun now(): Long = Clock.System.now().toEpochMilliseconds()
    override fun nowInstant(): Instant = Clock.System.now()
}
