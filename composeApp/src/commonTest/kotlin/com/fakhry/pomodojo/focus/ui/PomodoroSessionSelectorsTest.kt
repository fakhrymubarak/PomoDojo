package com.fakhry.pomodojo.focus.ui

import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.preferences.ui.model.TimerUi
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PomodoroSessionSelectorsTest {
    private val baseSegment =
        TimelineSegmentUi(
            type = TimerType.FOCUS,
            cycleNumber = 1,
            timer = TimerUi(progress = 0.25f),
        )
    private val baseTimeline =
        TimelineUiModel(
            segments = persistentListOf(baseSegment),
            hourSplits = persistentListOf(25),
        )
    private val baseState =
        PomodoroSessionUiState(
            totalCycle = 4,
            activeSegment = baseSegment,
            timeline = baseTimeline,
            quote = QuoteContent(text = "Stay focused"),
        )

    @Test
    fun headerSelectorIgnoresQuoteChanges() {
        val initialHeader = baseState.toHeaderUiState()
        val quoteUpdated =
            baseState.copy(
                quote = QuoteContent(text = "New thought"),
            )
        assertEquals(initialHeader, quoteUpdated.toHeaderUiState())

        val nextSegment = baseSegment.copy(cycleNumber = 2)
        val headerAfterSegmentChange = baseState.copy(activeSegment = nextSegment).toHeaderUiState()
        assertNotEquals(initialHeader, headerAfterSegmentChange)
        assertEquals(nextSegment, headerAfterSegmentChange.activeSegment)
    }

    @Test
    fun timelineSelectorUnchangedWhenOnlyQuoteUpdates() {
        val initialTimeline = baseState.timeline
        val quoteUpdated = baseState.copy(quote = QuoteContent(text = "Another one"))
        assertEquals(initialTimeline, quoteUpdated.timeline)

        val extendedTimeline = baseTimeline.copy(hourSplits = persistentListOf(25, 5))
        val stateWithNewTimeline = baseState.copy(timeline = extendedTimeline)
        assertNotEquals(initialTimeline, stateWithNewTimeline.timeline)
    }
}
