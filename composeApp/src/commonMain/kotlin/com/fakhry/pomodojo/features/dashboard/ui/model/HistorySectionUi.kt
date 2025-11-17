package com.fakhry.pomodojo.features.dashboard.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HistorySectionUi(
    val focusMinutesThisYear: String = "0",
    val selectedYear: Int = 0,
    val availableYears: ImmutableList<Int> = persistentListOf(),
    val cells: ImmutableList<ImmutableList<HistoryCell>> = persistentListOf(),
)

@Immutable
sealed class HistoryCell {
    @Immutable
    data class Text(val text: String) : HistoryCell()

    @Immutable
    data object Empty : HistoryCell()

    @Immutable
    data class GraphLevel(
        val intensityLevel: Int,
        val focusMinutes: Int,
        val breakMinutes: Int,
        val date: String = "",
    ) : HistoryCell()
}
