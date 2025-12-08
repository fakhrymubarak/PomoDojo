package com.fakhry.pomodojo.features.dashboard.ui.utils

import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FakeCurrentTimeProvider(private val year: Int = 2024) : CurrentTimeProvider {
    override fun now(): Long = 0L

    override fun nowInstant(): Instant {
        val millis = when (year) {
            2024 -> 1704067200000L // 2024-01-01 00:00:00 UTC
            2023 -> 1672531200000L // 2023-01-01 00:00:00 UTC
            else -> 0L
        }
        return Instant.Companion.fromEpochMilliseconds(millis)
    }
}
