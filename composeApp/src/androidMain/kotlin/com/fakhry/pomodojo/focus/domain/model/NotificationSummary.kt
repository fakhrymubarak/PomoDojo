package com.fakhry.pomodojo.focus.domain.model

internal data class NotificationSummary(
    val sessionId: String = "",
    val title: String = "",
    val timerText: String = "",
    val segmentProgressPercent: Int = 0,
    val isPaused: Boolean = false,
    val finishTimeMillis: Long,
    val quote: String = "",
    val isAllSegmentsCompleted: Boolean = false,
)
