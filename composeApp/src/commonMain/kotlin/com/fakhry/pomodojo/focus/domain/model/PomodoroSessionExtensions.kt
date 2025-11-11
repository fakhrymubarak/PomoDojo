package com.fakhry.pomodojo.focus.domain.model

/**
 * Stable identifier used for notifying platform-specific services.
 * Uses the session start timestamp because it is unique per run.
 */
fun PomodoroSessionDomain.sessionId(): String = startedAtEpochMs.toString()
