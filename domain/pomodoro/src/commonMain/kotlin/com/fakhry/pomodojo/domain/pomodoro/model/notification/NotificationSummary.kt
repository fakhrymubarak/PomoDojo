package com.fakhry.pomodojo.domain.pomodoro.model.notification

data class NotificationSummary(
    val sessionId: String = "",
    val title: String = "",
    val timerText: String = "",
    val segmentProgressPercent: Int = 0,
    val isPaused: Boolean = false,
    val finishTimeMillis: Long = 0,
    val quote: String = "",
    val isAllSegmentsCompleted: Boolean = false,
)
