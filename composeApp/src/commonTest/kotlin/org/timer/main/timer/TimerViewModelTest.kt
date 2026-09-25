package org.timer.main.timer

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.timer.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.settings.*
import org.timer.main.domain.statistic.*
import org.timer.main.domain.timer.*
import org.timer.main.timer.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsGateway: FakeSettingsGateway
    private lateinit var playAlarmUseCase: FakePlayAlarmUseCase
    private lateinit var cancelAlarmUseCase: FakeCancelAlarmUseCase
    private lateinit var mobileAlarm: FakeMobileAlarm
    private lateinit var timerFactory: FakeTimerFactory
    private lateinit var timerGateway: FakeTimerGateway
    private lateinit var getKittidorosUseCase: FakeGetKittidorosNumberUseCase
    private lateinit var incrementKittidoroUseCase: FakeIncrementKittdorsNumberUseCase
    private lateinit var resetKittidoroUseCase: FakeResetKittidoroNumberUseCase
    private lateinit var resetExercisesUseCase: FakeResetExercisesNumberUseCase
    private lateinit var viewModel: TimerViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        initFakes()
        buildViewModel(isMobile = true)
    }

    private fun initFakes() {
        settingsGateway = FakeSettingsGateway().apply {
            timeSettingsFlow.value =
                TimeSettings(0, 25L * 60 * 1000, 5L * 60 * 1000, 15L * 60 * 1000)
        }
        playAlarmUseCase = FakePlayAlarmUseCase()
        cancelAlarmUseCase = FakeCancelAlarmUseCase()
        mobileAlarm = FakeMobileAlarm()
        timerFactory = FakeTimerFactory()
        timerGateway = FakeTimerGateway()
        getKittidorosUseCase = FakeGetKittidorosNumberUseCase()
        incrementKittidoroUseCase = FakeIncrementKittdorsNumberUseCase(getKittidorosUseCase)
        resetKittidoroUseCase = FakeResetKittidoroNumberUseCase()
        resetExercisesUseCase = FakeResetExercisesNumberUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(isMobile: Boolean) {
        val startTimerUseCase = DefaultStartTimerUseCase(timerGateway)
        viewModel = TimerViewModel(
            settings = settingsGateway,
            playAlarmUseCase = playAlarmUseCase,
            cancelAlarmUseCase = cancelAlarmUseCase,
            mobileAlarm = mobileAlarm,
            timerFactory = timerFactory,
            incrementKittydoroNumberUseCase = incrementKittidoroUseCase,
            getKittidorosNumberUseCase = getKittidorosUseCase,
            resetKittidoroNumberUseCase = resetKittidoroUseCase,
            resetExercisesNumberUseCase = resetExercisesUseCase,
            startPomodoroUseCase = StartPomodoroUseCase(startTimerUseCase),
            startShortBreakUseCase = StartShortBreakUseCase(startTimerUseCase),
            startLongBreakUseCase = StartLongBreakUseCase(startTimerUseCase),
            stopTimerUseCase = DefaultStopTimerUseCase(timerGateway),
            isMobile = { isMobile },
        )
    }

    // The view model recreates its three timers (pomodoro, short, long) on every init;
    // the active ones are the most recently created triple.
    private fun pomodoroTimer() = timerFactory.createdTimers.let { it[it.size - 3] }
    private fun shortBreakTimer() = timerFactory.createdTimers.let { it[it.size - 2] }

    // ────────────────────────────── Initial state ──────────────────────────────

    @Test
    fun `initial state shows the configured times`() = runTest {
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.pomodoroTime).isEqualTo("25:00")
        assertThat(state.shortBreakTime).isEqualTo("05:00")
        assertThat(state.longBreakTime).isEqualTo("15:00")
        assertThat(state.timerState).isEqualTo(TimerState.Pomodoro)
    }

    // ────────────────────────────── Starting timers ──────────────────────────────

    @Test
    fun `starting the pomodoro runs the timer and shows a live notification`() = runTest {
        advanceUntilIdle()

        viewModel.onPomodoroStartClick()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.isPomodoroTimerRunning).isTrue()
        assertThat(pomodoroTimer().startCount).isEqualTo(1)
        assertThat(mobileAlarm.startLiveNotificationCount).isEqualTo(1)
        assertThat(mobileAlarm.scheduleCount).isEqualTo(1)
    }

    @Test
    fun `starting a short break selects the short break tab`() = runTest {
        advanceUntilIdle()

        viewModel.onShortBreakStartClick()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.selectedTabIndex).isEqualTo(1)
        assertThat(state.timerState).isEqualTo(TimerState.ShortBreak)
        assertThat(state.isShortBreakTimerRunning).isTrue()
        assertThat(mobileAlarm.lastStartLiveNotificationIsBreak).isEqualTo(true)
    }

    @Test
    fun `starting a long break selects the long break tab`() = runTest {
        advanceUntilIdle()

        viewModel.onLongBreakStartClick()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.selectedTabIndex).isEqualTo(2)
        assertThat(state.timerState).isEqualTo(TimerState.LongBreak)
        assertThat(state.isLongBreakTimerRunning).isTrue()
    }

    // ────────────────────────────── Tabs ──────────────────────────────

    @Test
    fun `changing the page updates the selected tab`() = runTest {
        advanceUntilIdle()

        viewModel.onPageChanged(2)
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.selectedTabIndex).isEqualTo(2)
    }

    @Test
    fun `tab clicks are emitted to listeners`() = runTest {
        advanceUntilIdle()
        val firstClick = async { viewModel.tabClicks.first() }
        runCurrent() // let the collector subscribe before we emit

        viewModel.onTabClick(2)

        assertThat(firstClick.await()).isEqualTo(2)
    }

    // ────────────────────────────── Reset ──────────────────────────────

    @Test
    fun `resetting returns to the pomodoro state and cancels the alarm`() = runTest {
        advanceUntilIdle()
        val cancelsBefore = cancelAlarmUseCase.cancelCount

        viewModel.onResetClick()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.timerState).isEqualTo(TimerState.Pomodoro)
        assertThat(cancelAlarmUseCase.cancelCount).isEqualTo(cancelsBefore + 1)
    }

    // ────────────────────────────── Finishing timers ──────────────────────────────

    @Test
    fun `on mobile finishing a pomodoro counts a kittidoro and asks to show activities without playing an alarm`() = runTest {
        advanceUntilIdle()

        pomodoroTimer().finish()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.kittyDoroNumber).isEqualTo(1)
        assertThat(viewModel.viewState.value.navigateToActivitiesScreen).isTrue()
        assertThat(playAlarmUseCase.playCount).isEqualTo(0)

        viewModel.onNavigatedToActivitiesScreen()
        assertThat(viewModel.viewState.value.navigateToActivitiesScreen).isFalse()
    }

    @Test
    fun `on web finishing a pomodoro plays the alarm instead of asking to show activities`() = runTest {
        initFakes()
        buildViewModel(isMobile = false)
        advanceUntilIdle()

        pomodoroTimer().finish()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.kittyDoroNumber).isEqualTo(1)
        assertThat(playAlarmUseCase.playCount).isEqualTo(1)
        assertThat(viewModel.viewState.value.navigateToActivitiesScreen).isFalse()
    }

    @Test
    fun `on mobile finishing a short break returns to the pomodoro tab without playing an alarm`() = runTest {
        advanceUntilIdle()
        viewModel.onShortBreakStartClick()
        advanceUntilIdle()

        shortBreakTimer().finish()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.selectedTabIndex).isEqualTo(0)
        assertThat(state.timerState).isEqualTo(TimerState.Pomodoro)
        assertThat(playAlarmUseCase.playCount).isEqualTo(0)
    }

    @Test
    fun `on web finishing a short break plays the alarm and then returns to the pomodoro tab`() = runTest {
        initFakes()
        buildViewModel(isMobile = false)
        advanceUntilIdle()
        viewModel.onShortBreakStartClick()
        advanceUntilIdle()

        shortBreakTimer().finish()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(playAlarmUseCase.playCount).isEqualTo(1)
        assertThat(state.selectedTabIndex).isEqualTo(0)
        assertThat(state.timerState).isEqualTo(TimerState.Pomodoro)
    }

    // ────────────────────────────── Lifecycle ──────────────────────────────

    @Test
    fun `resuming resets the daily kittidoro and exercise counters`() = runTest {
        advanceUntilIdle()

        viewModel.onResume()
        advanceUntilIdle()

        assertThat(resetKittidoroUseCase.invokeCount).isEqualTo(1)
        assertThat(resetExercisesUseCase.invokeCount).isEqualTo(1)
    }
}
