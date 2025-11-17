package com.fakhry.pomodojo.dashboard.ui.mapper

import com.fakhry.pomodojo.features.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.features.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.features.dashboard.ui.mapper.mapToHistorySectionUi
import com.fakhry.pomodojo.features.dashboard.ui.model.HistoryCell
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HistoryGraphMapperTest {
    private val historyDomain =
        PomodoroHistoryDomain(
            focusMinutesThisYear = 0,
            availableYears = emptyList(),
            histories =
            listOf(
                HistoryDomain(date = "2024-01-03", focusMinutes = 25, breakMinutes = 5),
                HistoryDomain(date = "2024-02-02", focusMinutes = 30, breakMinutes = 10),
            ),
        )

    @Test
    fun `weeks are ordered from left to right`() {
        val cells = historyDomain.mapToHistorySectionUi(selectedYear = 2024).cells
        val janRow = cells.drop(1).last()

        val label = janRow.first()
        assertTrue(label is HistoryCell.Text && label.text == "Jan")

        val janDates =
            janRow.drop(1).take(7).map { cell ->
                (cell as HistoryCell.GraphLevel).date
            }

        assertEquals(
            listOf("1 Jan", "2 Jan", "3 Jan", "4 Jan", "5 Jan", "6 Jan", "7 Jan"),
            janDates,
        )
    }

    @Test
    fun `rows spanning month boundaries show new month label`() {
        val cells = historyDomain.mapToHistorySectionUi(selectedYear = 2024).cells

        val febRow =
            cells
                .drop(1)
                .first { row ->
                    row.drop(1).any { cell ->
                        cell is HistoryCell.GraphLevel && cell.date == "1 Feb"
                    }
                }

        val label = febRow.first()
        assertTrue(label is HistoryCell.Text && label.text == "Feb")

        val expectedDates =
            listOf("29 Jan", "30 Jan", "31 Jan", "1 Feb", "2 Feb", "3 Feb", "4 Feb")
        val actualDates =
            febRow.drop(1).take(7).map { cell ->
                (cell as HistoryCell.GraphLevel).date
            }

        assertEquals(expectedDates, actualDates)
    }

    @Test
    fun `graph cells always include a date label`() {
        val cells = historyDomain.mapToHistorySectionUi(selectedYear = 2024).cells

        val graphCells =
            cells.flatMap { row ->
                row.filterIsInstance<HistoryCell.GraphLevel>()
            }

        assertTrue(graphCells.isNotEmpty())

        graphCells.forEach { cell ->
            assertTrue(cell.date.isNotBlank(), "Expected non-empty date for $cell")
        }
    }

    @Test
    fun `current year mapping stops at today`() {
        val today = LocalDate(year = 2024, month = Month.FEBRUARY, day = 3)
        val cells =
            historyDomain
                .mapToHistorySectionUi(
                    selectedYear = 2024,
                    currentDate = today,
                ).cells

        val recentRow =
            cells
                .drop(1)
                .first { row ->
                    row.drop(1).any { cell ->
                        cell is HistoryCell.GraphLevel && cell.date == "3 Feb"
                    }
                }
        val dayCells = recentRow.drop(1)
        val lastGraphIndex = dayCells.indexOfLast { it is HistoryCell.GraphLevel }

        assertTrue(lastGraphIndex >= 0)
        val lastGraph = dayCells[lastGraphIndex] as HistoryCell.GraphLevel
        assertEquals("3 Feb", lastGraph.date)
        assertTrue(dayCells.drop(lastGraphIndex + 1).all { it is HistoryCell.Empty })
        assertTrue(dayCells.filterIsInstance<HistoryCell.GraphLevel>().none { it.date == "4 Feb" })
    }

    @Test
    fun `january row aligns with actual weekday`() {
        val domain =
            PomodoroHistoryDomain(
                focusMinutesThisYear = 0,
                availableYears = listOf(2023),
                histories =
                listOf(
                    HistoryDomain(
                        date = "2023-01-01",
                        focusMinutes = 15,
                        breakMinutes = 5,
                    ),
                ),
            )

        val cells = domain.mapToHistorySectionUi(selectedYear = 2023).cells
        val janRow =
            cells
                .drop(1)
                .last { row ->
                    row.drop(1).any { cell ->
                        cell is HistoryCell.GraphLevel && cell.date == "1 Jan"
                    }
                }
        val dayCells = janRow.drop(1)
        val firstGraphIndex = dayCells.indexOfFirst { it is HistoryCell.GraphLevel }

        assertEquals(6, firstGraphIndex) // Sunday column (Mon-first)
        val label = janRow.first()
        assertTrue(label is HistoryCell.Text && label.text == "Jan")
    }
}
