package com.fakhry.pomodojo.core.notification.notifications

import android.content.Context
import com.fakhry.pomodojo.core.notification.R
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType

private const val MAX_PERCENT = 100

internal fun SessionNotice.Showing.title(context: Context): String = when (this) {
    is SessionNotice.AtGate -> context.getString(
        R.string.focus_session_awaiting_title_format,
        segment.type.toLabel(context),
    )

    else -> context.getString(
        R.string.focus_session_notification_body_format,
        segment.cycleNumber,
        totalCycle,
        segment.type.toLabel(context),
    )
}

/** One line on where the Segment stands, for surfaces that cannot show the quote. */
internal fun SessionNotice.Showing.status(context: Context): String = when (this) {
    is SessionNotice.Running -> context.getString(
        R.string.focus_session_notification_subtitle_format_running,
        remainingMs.formatDurationMillis(),
    )

    is SessionNotice.Paused -> context.getString(
        R.string.focus_session_notification_subtitle_format_paused,
        remainingMs.formatDurationMillis(),
    )

    is SessionNotice.AtGate -> context.getString(R.string.focus_session_awaiting_body)
}

internal fun SessionNotice.Showing.progressPercent(): Int {
    val duration = segment.durationMs
    if (duration <= 0L) return MAX_PERCENT
    return ((duration - remainingMs) * MAX_PERCENT / duration).toInt().coerceIn(0, MAX_PERCENT)
}

private fun TimerType.toLabel(context: Context): String = when (this) {
    TimerType.FOCUS -> context.getString(R.string.focus_session_phase_focus_label)
    TimerType.SHORT_BREAK -> context.getString(R.string.focus_session_phase_short_break_label)
    TimerType.LONG_BREAK -> context.getString(R.string.focus_session_phase_long_break_label)
}
