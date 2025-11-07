package com.fakhry.pomodojo.focus

import com.fakhry.pomodojo.focus.domain.model.FocusPhase
import com.fakhry.pomodojo.focus.domain.model.FocusSessionConfig
import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.model.SessionIdGenerator
import com.fakhry.pomodojo.focus.domain.repository.FocusSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.ui.FocusPomodoroUiState
import com.fakhry.pomodojo.focus.ui.FocusPomodoroViewModel
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FocusPomodoroViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var quoteRepository: FakeQuoteRepository
    private lateinit var sessionRepository: FakeFocusSessionRepository
    private lateinit var viewModel: FocusPomodoroViewModel
    private lateinit var timeProvider: FakeCurrentTimeProvider
    private lateinit var notifier: FakeFocusSessionNotifier

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        quoteRepository = FakeQuoteRepository()
        sessionRepository = FakeFocusSessionRepository()
        timeProvider = FakeCurrentTimeProvider()
        notifier = FakeFocusSessionNotifier()
        viewModel = FocusPomodoroViewModel(
            sessionRepository = sessionRepository,
            quoteRepository = quoteRepository,
            dispatcherProvider = DispatcherProvider(dispatcher),
            sessionIdGenerator = SessionIdGenerator { "session-1" },
            currentTimeProvider = timeProvider,
            notifier = notifier,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startNewSession_selectsRandomQuoteAndEmitsRunningState() = runTest(dispatcher) {
        val config = FocusSessionConfig(
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            totalCycles = 4,
            autoStartNextPhase = false,
            autoStartBreaks = false,
        )

        viewModel.startNewSession(config)

        val state = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertEquals(FocusTimerStatus.RUNNING, state.timerStatus)
        assertEquals(25 * 60, state.remainingSeconds)
        assertEquals(25 * 60, state.totalSeconds)
        assertEquals("Believe in yourself", state.quote.text)
        assertEquals("session-1", sessionRepository.lastSaved?.sessionId)
        assertEquals(0, state.completedSegments)
        assertEquals(4, state.totalSegments)
        assertEquals(FocusPhase.FOCUS, state.phase)
        assertTrue(notifier.scheduledIds.contains("session-1"))
    }

    @Test
    fun endConfirmationFlow_setsDialogAndCompletesSession() = runTest(dispatcher) {
        val config = FocusSessionConfig(
            focusDurationMinutes = 15,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            totalCycles = 4,
            autoStartNextPhase = false,
            autoStartBreaks = false,
        )

        viewModel.startNewSession(config)

        viewModel.onEndClicked()
        val confirmState = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertTrue(confirmState.showConfirmEndDialog)

        viewModel.onDismissConfirmEnd()
        val dismissedState = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertFalse(dismissedState.showConfirmEndDialog)

        viewModel.onEndClicked()
        viewModel.onConfirmFinish()

        advanceUntilIdle()

        assertIs<FocusPomodoroUiState.Completed>(viewModel.state.value)
        assertTrue(sessionRepository.completeCalled)
        assertTrue(notifier.cancelledIds.contains("session-1"))
    }

    @Test
    fun decrementTimer_updatesRemainingSecondsWhenRunning() = runTest(dispatcher) {
        val config = FocusSessionConfig(
            focusDurationMinutes = 1,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            totalCycles = 4,
            autoStartNextPhase = false,
            autoStartBreaks = false,
        )

        viewModel.startNewSession(config)
        timeProvider.nowValue = 1L

        viewModel.decrementTimer()

        val state = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertEquals(59, state.remainingSeconds)
    }

    @Test
    fun decrementTimer_completesSessionWhenTimeElapses() = runTest(dispatcher) {
        val restoredQuote = QuoteContent(
            id = "quote-2",
            text = "Stay focused",
            character = "Tanjiro",
            sourceTitle = "Demon Slayer",
            metadata = "Season 1"
        )
        sessionRepository.activeSession = FocusSessionSnapshot(
            sessionId = "existing",
            status = FocusTimerStatus.RUNNING,
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            autoStartNextPhase = false,
            autoStartBreaks = false,
            phaseRemainingSeconds = 1,
            currentPhaseTotalSeconds = 1,
            completedCycles = 0,
            totalCycles = 4,
            phase = FocusPhase.FOCUS,
            quote = restoredQuote,
            phaseStartedAtEpochMs = 0L,
            startedAtEpochMs = 0L,
            updatedAtEpochMs = 0L,
        )

        timeProvider = FakeCurrentTimeProvider()
        notifier = FakeFocusSessionNotifier()
        viewModel = FocusPomodoroViewModel(
            sessionRepository = sessionRepository,
            quoteRepository = quoteRepository,
            dispatcherProvider = DispatcherProvider(dispatcher),
            sessionIdGenerator = SessionIdGenerator { "session-3" },
            currentTimeProvider = timeProvider,
            notifier = notifier,
        )

        advanceUntilIdle()
        timeProvider.nowValue = 2L

        viewModel.decrementTimer()
        advanceUntilIdle()

        assertIs<FocusPomodoroUiState.Completed>(viewModel.state.value)
        assertTrue(sessionRepository.completeCalled)
    }

    @Test
    fun restoreRunningSession_updatesRemainingAndSchedulesNotifier() = runTest(dispatcher) {
        val restoredQuote = QuoteContent(
            id = "quote-3",
            text = "Keep pushing",
            character = "Izuku Midoriya",
            sourceTitle = "My Hero Academia",
            metadata = null,
        )
        sessionRepository.activeSession = FocusSessionSnapshot(
            sessionId = "restore-running",
            status = FocusTimerStatus.RUNNING,
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            autoStartNextPhase = false,
            autoStartBreaks = false,
            phaseRemainingSeconds = 120,
            currentPhaseTotalSeconds = 150,
            completedCycles = 0,
            totalCycles = 4,
            phase = FocusPhase.FOCUS,
            quote = restoredQuote,
            phaseStartedAtEpochMs = 0L,
            startedAtEpochMs = 0L,
            updatedAtEpochMs = 0L,
        )

        timeProvider = FakeCurrentTimeProvider().apply { nowValue = 60_000L }
        notifier = FakeFocusSessionNotifier()
        viewModel = FocusPomodoroViewModel(
            sessionRepository = sessionRepository,
            quoteRepository = quoteRepository,
            dispatcherProvider = DispatcherProvider(dispatcher),
            sessionIdGenerator = SessionIdGenerator { "session-restore" },
            currentTimeProvider = timeProvider,
            notifier = notifier,
        )

        advanceUntilIdle()

        val state = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertEquals(60, state.remainingSeconds)
        assertEquals("restore-running", notifier.scheduledIds.last())
        assertEquals(60, sessionRepository.lastUpdated?.phaseRemainingSeconds)
    }

    @Test
    fun restoreRunningSession_autoCompletesWhenExpired() = runTest(dispatcher) {
        sessionRepository.activeSession = FocusSessionSnapshot(
            sessionId = "restore-expired",
            status = FocusTimerStatus.RUNNING,
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            autoStartNextPhase = false,
            autoStartBreaks = false,
            phaseRemainingSeconds = 5,
            currentPhaseTotalSeconds = 100,
            completedCycles = 0,
            totalCycles = 4,
            phase = FocusPhase.FOCUS,
            quote = null,
            phaseStartedAtEpochMs = 0L,
            startedAtEpochMs = 0L,
            updatedAtEpochMs = 0L,
        )

        timeProvider = FakeCurrentTimeProvider().apply { nowValue = 10_000L }
        notifier = FakeFocusSessionNotifier()
        viewModel = FocusPomodoroViewModel(
            sessionRepository = sessionRepository,
            quoteRepository = quoteRepository,
            dispatcherProvider = DispatcherProvider(dispatcher),
            sessionIdGenerator = SessionIdGenerator { "session-expired" },
            currentTimeProvider = timeProvider,
            notifier = notifier,
        )

        advanceUntilIdle()

        assertIs<FocusPomodoroUiState.Completed>(viewModel.state.value)
        assertTrue(sessionRepository.completeCalled)
        assertTrue(notifier.cancelledIds.contains("restore-expired"))
    }

    @Test
    fun restoreExistingPausedSession_emitsPausedState() = runTest(dispatcher) {
        val restoredQuote = QuoteContent(
            id = "quote-2",
            text = "Stay focused",
            character = "Tanjiro",
            sourceTitle = "Demon Slayer",
            metadata = "Season 1"
        )
        sessionRepository.activeSession = FocusSessionSnapshot(
            sessionId = "existing",
            status = FocusTimerStatus.PAUSED,
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 10,
            autoStartNextPhase = false,
            autoStartBreaks = false,
            phaseRemainingSeconds = 90,
            currentPhaseTotalSeconds = 300,
            completedCycles = 2,
            totalCycles = 4,
            phase = FocusPhase.FOCUS,
            quote = restoredQuote,
            phaseStartedAtEpochMs = 0L,
            startedAtEpochMs = 0L,
            updatedAtEpochMs = 0L,
        )

        notifier = FakeFocusSessionNotifier()
        viewModel = FocusPomodoroViewModel(
            sessionRepository = sessionRepository,
            quoteRepository = quoteRepository,
            dispatcherProvider = DispatcherProvider(dispatcher),
            sessionIdGenerator = SessionIdGenerator { "session-2" },
            currentTimeProvider = timeProvider,
            notifier = notifier,
        )

        advanceUntilIdle()

        val state = assertIs<FocusPomodoroUiState.Active>(viewModel.state.value)
        assertEquals(FocusTimerStatus.PAUSED, state.timerStatus)
        assertEquals(90, state.remainingSeconds)
        assertEquals(restoredQuote.text, state.quote.text)
        assertEquals(2, state.completedSegments)
        assertEquals(4, state.totalSegments)
    }

    private class FakeQuoteRepository : QuoteRepository {
        var quotes: MutableList<QuoteContent> = mutableListOf(
            QuoteContent(
                id = "quote-1",
                text = "Believe in yourself",
                character = "Hinata Shoyo",
                sourceTitle = "Haikyuu!!",
                metadata = "Season 1"
            ),
            QuoteContent(
                id = "quote-2",
                text = "Stay focused",
                character = "Tanjiro Kamado",
                sourceTitle = "Demon Slayer",
                metadata = "Season 1"
            ),
        )

        override suspend fun randomQuote(): QuoteContent? = quotes.firstOrNull()

        override suspend fun getById(id: String): QuoteContent? =
            quotes.firstOrNull { it.id == id }
    }

    private class FakeCurrentTimeProvider : CurrentTimeProvider {
        var nowValue: Long = 0L
        override fun now(): Long = nowValue
    }

    private class FakeFocusSessionRepository : FocusSessionRepository {
        var lastSaved: FocusSessionSnapshot? = null
        var lastUpdated: FocusSessionSnapshot? = null
        var completedSnapshot: FocusSessionSnapshot? = null
        var completeCalled: Boolean = false
        var activeSession: FocusSessionSnapshot? = null

        override suspend fun getActiveSession(): FocusSessionSnapshot? = activeSession

        override suspend fun saveActiveSession(snapshot: FocusSessionSnapshot) {
            activeSession = snapshot
            lastSaved = snapshot
        }

        override suspend fun updateActiveSession(snapshot: FocusSessionSnapshot) {
            activeSession = snapshot
            lastUpdated = snapshot
        }

        override suspend fun completeSession(snapshot: FocusSessionSnapshot) {
            activeSession = null
            completeCalled = true
            completedSnapshot = snapshot
        }

        override suspend fun clearActiveSession() {
            activeSession = null
        }
    }

    private class FakeFocusSessionNotifier : FocusSessionNotifier {
        val scheduledIds = mutableListOf<String>()
        val cancelledIds = mutableListOf<String>()

        override suspend fun schedule(snapshot: FocusSessionSnapshot) {
            scheduledIds += snapshot.sessionId
        }

        override suspend fun cancel(sessionId: String) {
            cancelledIds += sessionId
        }
    }
}
