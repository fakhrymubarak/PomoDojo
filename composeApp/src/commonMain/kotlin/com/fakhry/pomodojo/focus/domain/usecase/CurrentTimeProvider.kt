package com.fakhry.pomodojo.focus.domain.usecase

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun interface CurrentTimeProvider {
    fun now(): Long
}

object SystemCurrentTimeProvider : CurrentTimeProvider {
    @OptIn(ExperimentalTime::class)
    override fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
