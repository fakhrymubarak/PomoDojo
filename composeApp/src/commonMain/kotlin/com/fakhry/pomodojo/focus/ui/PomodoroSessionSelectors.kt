package com.fakhry.pomodojo.focus.ui

import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun PomodoroSessionUiState.toHeaderUiState(): PomodoroHeaderUiState =
    PomodoroHeaderUiState(totalCycle = totalCycle, activeSegment = activeSegment)

fun Flow<PomodoroSessionUiState>.headerUiStateFlow(): Flow<PomodoroHeaderUiState> =
    map { it.toHeaderUiState() }.distinctUntilChanged()

fun Flow<PomodoroSessionUiState>.activeSegmentFlow(): Flow<TimelineSegmentUi> =
    map { it.activeSegment }.distinctUntilChanged()

fun Flow<PomodoroSessionUiState>.timelineFlow(): Flow<TimelineUiModel> =
    map { it.timeline }.distinctUntilChanged()

fun Flow<PomodoroSessionUiState>.quoteFlow(): Flow<QuoteContent> =
    map { it.quote }.distinctUntilChanged()
