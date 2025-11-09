package com.fakhry.pomodojo.focus

import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.GetActivePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.ui.FocusPomodoroViewModel
import com.fakhry.pomodojo.focus.ui.PhaseTimerStatus
import com.fakhry.pomodojo.focus.ui.PhaseType
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FocusPomodoroViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val dispatcherProvider = DispatcherProvider(dispatcher)
    private lateinit var repository: FakePomodoroSessionRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var quoteRepository: FakeQuoteRepository
    private lateinit var notifier: FakeFocusSessionNotifier
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var createUseCase: CreatePomodoroSessionUseCase
    private lateinit var getUseCase: GetActivePomodoroSessionUseCase
    private lateinit var viewModel: FocusPomodoroViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakePomodoroSessionRepository()
        preferencesRepository = FakePreferencesRepository()
        quoteRepository = FakeQuoteRepository()
        notifier = FakeFocusSessionNotifier()
        timeProvider = FakeTimeProvider()
        createUseCase = CreatePomodoroSessionUseCase(
            quoteRepo = quoteRepository,
            preferencesRepo = preferencesRepository,
            pomodoroSessionRepo = repository,
            dispatcher = dispatcherProvider,
        )
        getUseCase = GetActivePomodoroSessionUseCase(
            quoteRepo = quoteRepository,
            preferencesRepo = preferencesRepository,
            pomodoroSessionRepo = repository,
            dispatcher = dispatcherProvider,
        )
        viewModel = buildViewModel()
    }

    @AfterTest
    fun tearDown() {
        viewModel.stopTickerForTests()
        Dispatchers.resetMain()
    }

    @Test
    fun startSession_buildsInitialFocusPhase() = runViewModelTest {
        runCurrent()

        val state = viewModel.state.value
        assertEquals(3, state.phases.size)
        assertEquals(2, state.totalCycle)
        assertEquals(PhaseType.FOCUS, state.activeSegment.type)
        assertEquals(PhaseTimerStatus.RUNNING, state.activeSegment.timerStatus)
        assertEquals("01:00", state.activeSegment.formattedTime)
    }

    @Test
    fun togglePauseResume_updatesStatusAndPersists() = runViewModelTest {
        runCurrent()

        viewModel.togglePauseResume()
        runCurrent()

        var state = viewModel.state.value
        assertEquals(PhaseTimerStatus.PAUSED, state.activeSegment.timerStatus)
        assertEquals(FocusTimerStatus.PAUSED, repository.updatedSnapshot?.sessionStatus)
        assertTrue(notifier.cancelledIds.contains(repository.updatedSnapshot?.sessionId?.toString()))

        val startTime = state.startedAtEpochMs
        val focusDurationMs = preferencesRepository.current.focusMinutes * 60_000L
        timeProvider.nowValue = startTime + focusDurationMs + 1_000L

        advanceTimeBy(2_000L)
        runCurrent()

        state = viewModel.state.value
        assertEquals(PhaseType.FOCUS, state.activeSegment.type)
        assertEquals(PhaseTimerStatus.PAUSED, state.activeSegment.timerStatus)

        timeProvider.nowValue = startTime + 1_000L

        viewModel.togglePauseResume()
        runCurrent()

        state = viewModel.state.value
        assertEquals(PhaseTimerStatus.RUNNING, state.activeSegment.timerStatus)
        assertEquals(FocusTimerStatus.RUNNING, repository.updatedSnapshot?.sessionStatus)
        assertTrue(notifier.scheduledIds.contains(repository.updatedSnapshot?.sessionId?.toString()))
        assertTrue(repository.updatedSnapshot!!.elapsedPauseEpochMs > 0)
    }

    @Test
    fun decrementTimer_advancesActivePhaseWhenElapsedSurpassesDuration() = runViewModelTest {
        runCurrent()

        val startTime = viewModel.state.value.startedAtEpochMs
        val focusDurationMs = preferencesRepository.current.focusMinutes * 60_000L
        timeProvider.nowValue = startTime + focusDurationMs + 1_000L

        viewModel.decrementTimer()
        runCurrent()

        val state = viewModel.state.value
        assertEquals(PhaseType.SHORT_BREAK, state.activeSegment.type)
        assertEquals(PhaseTimerStatus.RUNNING, state.activeSegment.timerStatus)
    }

    @Test
    fun tickerAutomaticallyAdvancesPhase() = runViewModelTest {
        runCurrent()

        val startTime = viewModel.state.value.startedAtEpochMs
        val focusDurationMs = preferencesRepository.current.focusMinutes * 60_000L
        timeProvider.nowValue = startTime + focusDurationMs + 1_000L

        advanceTimeBy(1_000L)
        runCurrent()

        val state = viewModel.state.value
        assertEquals(PhaseType.SHORT_BREAK, state.activeSegment.type)
    }

    @Test
    fun confirmFinish_completesSessionAndSetsCompletionState() = runViewModelTest {
        runCurrent()

        viewModel.onEndClicked()
        assertTrue(viewModel.state.value.isShowConfirmEndDialog)

        viewModel.onConfirmFinish()
        runCurrent()

        assertTrue(viewModel.isComplete.value)
        assertFalse(viewModel.state.value.isShowConfirmEndDialog)
        assertEquals(repository.completedSnapshot?.sessionId, repository.lastSavedSnapshot?.sessionId)
    }

    private fun buildViewModel(): FocusPomodoroViewModel = FocusPomodoroViewModel(
        pomodoroSessionRepo = repository,
        currentTimeProvider = timeProvider,
        createPomodoroSessionUseCase = createUseCase,
        getPomodoroSessionUseCase = getUseCase,
        dispatcher = dispatcherProvider,
    )

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModel.stopTickerForTests()
        }
    }

    private class FakePomodoroSessionRepository : PomodoroSessionRepository {
        private var hasActive = false
        private var activeSession: ActiveFocusSessionDomain = ActiveFocusSessionDomain()
        var lastSavedSnapshot: ActiveFocusSessionDomain? = null
        var updatedSnapshot: ActiveFocusSessionDomain? = null
        var completedSnapshot: ActiveFocusSessionDomain? = null

        override suspend fun hasActiveSession(): Boolean = hasActive

        override suspend fun getActiveSession(): ActiveFocusSessionDomain = activeSession

        override suspend fun saveActiveSession(snapshot: ActiveFocusSessionDomain) {
            hasActive = true
            activeSession = snapshot
            lastSavedSnapshot = snapshot
        }

        override suspend fun updateActiveSession(snapshot: ActiveFocusSessionDomain) {
            activeSession = snapshot
            updatedSnapshot = snapshot
        }

        override suspend fun completeSession(snapshot: ActiveFocusSessionDomain) {
            hasActive = false
            completedSnapshot = snapshot
            activeSession = snapshot
        }

        override suspend fun clearActiveSession() {
            hasActive = false
            activeSession = ActiveFocusSessionDomain()
        }
    }

    private class FakeQuoteRepository : QuoteRepository {
        private val quote = QuoteContent(
            id = "quote-test",
            text = "Stay sharp",
            character = "Shinichi",
            sourceTitle = "Detective Conan",
            metadata = null,
        )

        override suspend fun randomQuote(): QuoteContent = quote
        override suspend fun getById(id: String): QuoteContent = quote
    }

    private class FakePreferencesRepository(
        initial: PreferencesDomain = PreferencesDomain(
            repeatCount = 2,
            focusMinutes = 1,
            breakMinutes = 1,
            longBreakEnabled = false,
            longBreakAfter = 2,
            longBreakMinutes = 2,
        ),
    ) : PreferencesRepository {
        private val mutableFlow = MutableStateFlow(initial)
        val current: PreferencesDomain get() = mutableFlow.value
        override val preferences: Flow<PreferencesDomain> = mutableFlow.asStateFlow()

        override suspend fun updateRepeatCount(value: Int) {
            mutableFlow.value = mutableFlow.value.copy(repeatCount = value)
        }

        override suspend fun updateFocusMinutes(value: Int) {
            mutableFlow.value = mutableFlow.value.copy(focusMinutes = value)
        }

        override suspend fun updateBreakMinutes(value: Int) {
            mutableFlow.value = mutableFlow.value.copy(breakMinutes = value)
        }

        override suspend fun updateLongBreakEnabled(enabled: Boolean) {
            mutableFlow.value = mutableFlow.value.copy(longBreakEnabled = enabled)
        }

        override suspend fun updateLongBreakAfter(value: Int) {
            mutableFlow.value = mutableFlow.value.copy(longBreakAfter = value)
        }

        override suspend fun updateLongBreakMinutes(value: Int) {
            mutableFlow.value = mutableFlow.value.copy(longBreakMinutes = value)
        }

        override suspend fun updateAppTheme(theme: com.fakhry.pomodojo.preferences.domain.model.AppTheme) {
            mutableFlow.value = mutableFlow.value.copy(appTheme = theme)
        }
    }

    private class FakeFocusSessionNotifier : FocusSessionNotifier {
        val scheduledIds = mutableListOf<String>()
        val cancelledIds = mutableListOf<String>()

        override suspend fun schedule(snapshot: ActiveFocusSessionDomain) {
            scheduledIds += snapshot.sessionId.toString()
        }

        override suspend fun cancel(sessionId: String) {
            cancelledIds += sessionId
        }
    }

    private class FakeTimeProvider : CurrentTimeProvider {
        var nowValue: Long = 0L
        override fun now(): Long = nowValue
    }
}
