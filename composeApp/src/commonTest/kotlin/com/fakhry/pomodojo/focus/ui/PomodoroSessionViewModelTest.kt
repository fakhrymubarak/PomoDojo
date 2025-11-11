package com.fakhry.pomodojo.focus.ui

import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroSessionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `session starts immediately with active focus segment`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        val state = viewModel.awaitSessionStarted()

        assertEquals(1, state.totalCycle, "state=$state")
        assertEquals(TimerStatusDomain.Running, state.activeSegment.timerStatus, "state=$state")
        assertEquals("01:00", state.activeSegment.timer.formattedTime, "state=$state")
        assertEquals(0f, state.activeSegment.timer.progress, "state=$state")
    }

    @Test
    fun `running timer updates remaining time and progress`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.awaitSessionStarted()

        advanceTimeBy(45_000)
        runCurrent()

        val state = viewModel.currentSnapshot()
        assertEquals(0.75f, state.activeSegment.timer.progress, 0.05f, "state=$state")
    }

    @Test
    fun `confirm finish emits dialog and completion side effects`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.awaitSessionStarted()

        val showDialog =
            async {
                viewModel.container.sideEffectFlow.first {
                    it is PomodoroSessionSideEffect.ShowEndSessionDialog && it.isShown
                } as PomodoroSessionSideEffect.ShowEndSessionDialog
            }
        viewModel.onEndClicked()
        assertTrue(showDialog.await().isShown)

        val hideDialog =
            async {
                viewModel.container.sideEffectFlow.first {
                    it is PomodoroSessionSideEffect.ShowEndSessionDialog && !it.isShown
                } as PomodoroSessionSideEffect.ShowEndSessionDialog
            }
        viewModel.onDismissConfirmEnd()
        assertTrue(!hideDialog.await().isShown)

        val completeEffect =
            async {
                viewModel.container.sideEffectFlow.first {
                    it is PomodoroSessionSideEffect.OnSessionComplete
                }
            }
        viewModel.onConfirmFinish()
        assertTrue(completeEffect.await() is PomodoroSessionSideEffect.OnSessionComplete)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createViewModel(
        preferences: PreferencesDomain =
            PreferencesDomain(
                repeatCount = 1,
                focusMinutes = 1,
                breakMinutes = 1,
                longBreakEnabled = false,
                longBreakAfter = PreferencesDomain.DEFAULT_LONG_BREAK_AFTER,
                longBreakMinutes = 1,
            ),
    ): PomodoroSessionViewModel {
        val currentTimeProvider = TestCurrentTimeProvider(testScheduler)
        val quoteRepository = FakeQuoteRepository()
        val preferencesRepository = FakePreferencesRepository(preferences)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val sessionRepository = FakePomodoroSessionRepository()
        val createSessionUseCase =
            CreatePomodoroSessionUseCase(
                sessionRepository = sessionRepository,
                quoteRepo = quoteRepository,
                preferencesRepo = preferencesRepository,
                timelineBuilder = BuildTimerSegmentsUseCase(),
                hourSplitter = BuildHourSplitTimelineUseCase(),
                dispatcher = dispatcherProvider,
            )

        return PomodoroSessionViewModel(
            currentTimeProvider = currentTimeProvider,
            createPomodoroSessionUseCase = createSessionUseCase,
            sessionRepository = sessionRepository,
            dispatcher = dispatcherProvider,
        )
    }
}

private suspend fun PomodoroSessionViewModel.awaitSessionStarted(): PomodoroSessionUiState =
    container.stateFlow.first { it.totalCycle > 0 }

private fun PomodoroSessionViewModel.currentSnapshot(): PomodoroSessionUiState =
    container.stateFlow.value

private class FakeQuoteRepository(
    private val quote: QuoteContent = QuoteContent(
        id = "quote-id",
        text = "Stay focused",
        character = "Sensei",
    ),
) : QuoteRepository {
    override suspend fun randomQuote(): QuoteContent = quote

    override suspend fun getById(id: String): QuoteContent = quote
}

private class FakePreferencesRepository(initial: PreferencesDomain) : PreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val preferences: Flow<PreferencesDomain> = state.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        state.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        state.update { it.copy(focusMinutes = value) }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        state.update { it.copy(breakMinutes = value) }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        state.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        state.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        state.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAppTheme(theme: AppTheme) {
        state.update { it.copy(appTheme = theme) }
    }
}

private class FakePomodoroSessionRepository : com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository {
    override suspend fun getActiveSession(): com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain =
        throw UnsupportedOperationException("Not required for tests")

    override suspend fun saveActiveSession(snapshot: com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain) = Unit

    override suspend fun updateActiveSession(snapshot: com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain) = Unit

    override suspend fun completeSession(snapshot: com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain) = Unit

    override suspend fun clearActiveSession() = Unit

    override suspend fun hasActiveSession(): Boolean = false
}

@OptIn(ExperimentalTime::class)
private class TestCurrentTimeProvider(private val scheduler: TestCoroutineScheduler) :
    CurrentTimeProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now(): Long = scheduler.currentTime

    override fun nowInstant(): Instant = Instant.fromEpochMilliseconds(now())
}
