package com.fakhry.pomodojo.focus.domain.model

internal data class NotificationSummary(
    val sessionId: String,
    val title: String,
    val timerText: String,
    val segmentProgressPercent: Int,
    val isPaused: Boolean,
    val finishTimeMillis: Long,
)
