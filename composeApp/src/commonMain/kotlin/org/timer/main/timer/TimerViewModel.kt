package org.timer.main.timer

import androidx.lifecycle.*
import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import org.timer.main.*
import org.timer.main.domain.settings.*
import org.timer.main.domain.timer.*
import org.timer.main.isMobile as isMobilePlatform

private const val ITERATIONS_IN_ONE_CYCLE = 4

class TimerViewModel(
    private val settings: SettingsGateway,
    private val playAlarmUseCase: PlayAlarmUseCase,
    private val cancelAlarmUseCase: CancelAlarmUseCase,
    private val mobileAlarm: MobileAlarm,
    private val timerFactory: TimerFactory,
    private val incrementKittydoroNumberUseCase: IncrementKittdorsNumberUseCase,
    private val getKittidorosNumberUseCase: GetKittidorosNumberUseCase,
    private val resetKittidoroNumberUseCase: ResetKittidoroNumberUseCase,
    private val resetExercisesNumberUseCase: ResetExercisesNumberUseCase,
    private val startPomodoroUseCase: StartPomodoroUseCase,
    private val startShortBreakUseCase: StartShortBreakUseCase,
    private val startLongBreakUseCase: StartLongBreakUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val isMobile: () -> Boolean = ::isMobilePlatform
) : ViewModel() {
    private val _viewState = MutableStateFlow(TimerViewState())
    val viewState: StateFlow<TimerViewState> = _viewState.asStateFlow()

    private val _tabClicks = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val tabClicks = _tabClicks.asSharedFlow()

    private lateinit var pomodoroTimer: CountDownTimer
    private lateinit var shortBreakTimer: CountDownTimer
    private lateinit var longBreakTimer: CountDownTimer


    init {
        viewModelScope.launch {
            initTimers()
        }

        settings.getTimeSettings()
            .onEach { resetTimers() }
            .launchIn(viewModelScope)

        getKittidorosNumberUseCase()
            .onEach { kittyDoroNumber ->
                _viewState.update { it.copy(kittyDoroNumber = kittyDoroNumber) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initTimers() {
        val settings = settings.getTimeSettings().first()
        pomodoroTimer = timerFactory.createTimer(
            settings.pomodoroTime,
            onTick = ::onPomodoroTick,
            onFinish = ::onPomodoroFinish,
            isRunning = { isRunning -> _viewState.update { it.copy(isPomodoroTimerRunning = isRunning) } }

        )
        shortBreakTimer = timerFactory.createTimer(
            settings.shortBreakTime,
            onTick = ::onShortBreakTick,
            onFinish = ::onShortBreakFinish,
            isRunning = { isRunning -> _viewState.update { it.copy(isShortBreakTimerRunning = isRunning) } }
        )
        longBreakTimer = timerFactory.createTimer(
            settings.longBreakTime,
            onTick = ::onLongBreakTick,
            onFinish = ::onLongBreakFinish,
            isRunning = { isRunning -> _viewState.update { it.copy(isLongBreakTimerRunning = isRunning) } }
        )

        _viewState.update {
            it.copy(
                pomodoroTime = settings.pomodoroTime.formatToMMSS(),
                shortBreakTime = settings.shortBreakTime.formatToMMSS(),
                longBreakTime = settings.longBreakTime.formatToMMSS(),
            )
        }
    }

    private fun onShortBreakFinish() = viewModelScope.launch {
        if (isMobile()) {
            navigateToPomodoroTab()
        } else {
            playAlarmUseCase.invoke(onEnded = ::navigateToPomodoroTab)
        }
        stopTimerUseCase()
    }

    private fun onLongBreakFinish() = viewModelScope.launch {
        if (isMobile()) {
            navigateToPomodoroTab()
        } else {
            playAlarmUseCase.invoke(onEnded = ::navigateToPomodoroTab)
        }
        stopTimerUseCase()
    }


    private fun navigateToPomodoroTab() = viewModelScope.launch {
        resetAllTimers()
        _viewState.update {
            it.copy(
                selectedTabIndex = 0,
                timerState = TimerState.Pomodoro,
            )
        }
    }

    private var timerJob: Job? = null

    private fun onPomodoroFinish() = viewModelScope.launch {
        incrementKittydoroNumberUseCase()
        if (isMobile()) {
            navigateToBreakTab()
            _viewState.update { it.copy(navigateToActivitiesScreen = true) }
        } else {
            playAlarmUseCase.invoke(onEnded = {
                navigateToBreakTab()
            })
        }
        stopTimerUseCase()
    }

    private fun navigateToBreakTab() {
        if (viewState.value.kittyDoroNumber % ITERATIONS_IN_ONE_CYCLE == 0) {
            navigateToLongBreakTab()
        } else {
            navigateToShortBreakTab()
        }
    }

    private fun navigateToShortBreakTab() {
        resetAllTimers()
        _viewState.update {
            it.copy(
                selectedTabIndex = 1,
                timerState = TimerState.ShortBreak,
            )
        }
    }

    private fun navigateToLongBreakTab() {
        resetAllTimers()
        _viewState.update {
            it.copy(
                selectedTabIndex = 2,
                timerState = TimerState.LongBreak,
            )
        }
    }

    private fun resetAllTimers() {
        pomodoroTimer.resetTimer()
        longBreakTimer.resetTimer()
        shortBreakTimer.resetTimer()
    }

    private fun onPomodoroTick(millis: Long) {
        _viewState.update { it.copy(pomodoroTime = millis.formatToMMSS()) }
    }

    private fun onShortBreakTick(millis: Long) {
        _viewState.update { it.copy(shortBreakTime = millis.formatToMMSS()) }
    }

    private fun onLongBreakTick(millis: Long) {
        _viewState.update { it.copy(longBreakTime = millis.formatToMMSS()) }
    }

    fun onPomodoroStartClick() = viewModelScope.launch {
        shortBreakTimer.resetTimer()
        longBreakTimer.resetTimer()
        if (!_viewState.value.isPomodoroTimerRunning) {
            mobileAlarm.startLiveNotification(
                "Tomato " + (viewState.value.kittyDoroNumber + 1),
                false,
                pomodoroTimer.getCurrentTimeMillis(),
                settings.getAlarmSound().first()
            )

            scheduleAlarm(
                pomodoroTimer.getCurrentTimeMillis(),
                "Kittidoro Finished!",
                "Take a break Kitty"
            )
            pomodoroTimer.startTimer()
        }
        if (_viewState.value.timerState !is TimerState.Pomodoro) {
            _viewState.update {
                it.copy(
                    selectedTabIndex = 0,
                    timerState = TimerState.Pomodoro
                )
            }
        }
        startPomodoroUseCase(pomodoroTimer.getCurrentTimeMillis())
    }

    private suspend fun scheduleAlarm(currentTimerMillis: Long, title: String, body: String) {
        mobileAlarm.cancel()
        val scheduleDate = Clock.System.now().toEpochMilliseconds() + currentTimerMillis
        val alarmSound = settings.getAlarmSound().first()
        mobileAlarm.schedule(scheduleDate, alarmSound, title, body)
    }

    fun onShortBreakStartClick() = viewModelScope.launch {
        pomodoroTimer.resetTimer()
        longBreakTimer.resetTimer()
        if (!_viewState.value.isShortBreakTimerRunning) {
            mobileAlarm.startLiveNotification(
                "Short break",
                true,
                shortBreakTimer.getCurrentTimeMillis(),
                settings.getAlarmSound().first()
            )
            scheduleAlarm(
                shortBreakTimer.getCurrentTimeMillis(),
                "Short break finished",
                "Keep the eye on the ball!"
            )
            shortBreakTimer.startTimer()
        }
        if (_viewState.value.timerState !is TimerState.ShortBreak) {
            _viewState.update {
                it.copy(
                    selectedTabIndex = 1,
                    timerState = TimerState.ShortBreak
                )
            }
        }
        startShortBreakUseCase(shortBreakTimer.getCurrentTimeMillis())
    }

    fun onLongBreakStartClick() = viewModelScope.launch {
        pomodoroTimer.resetTimer()
        shortBreakTimer.resetTimer()
        if (!_viewState.value.isLongBreakTimerRunning) {
            mobileAlarm.startLiveNotification(
                "Long break",
                true,
                longBreakTimer.getCurrentTimeMillis(),
                settings.getAlarmSound().first()
            )

            scheduleAlarm(
                longBreakTimer.getCurrentTimeMillis(),
                "Long break finished",
                "Keep the eye on the ball!"
            )
            longBreakTimer.startTimer()
        }
        if (_viewState.value.timerState !is TimerState.LongBreak) {
            _viewState.update {
                it.copy(
                    selectedTabIndex = 2,
                    timerState = TimerState.LongBreak
                )
            }
        }
        startLongBreakUseCase(longBreakTimer.getCurrentTimeMillis())
    }

    fun onResetClick() = viewModelScope.launch {
        resetTimers()
    }

    private fun resetTimers() = viewModelScope.launch {
        timerJob?.cancel()
        cancelAlarmUseCase.invoke()
        if (::pomodoroTimer.isInitialized) pomodoroTimer.pauseTimer()
        if (::shortBreakTimer.isInitialized) shortBreakTimer.pauseTimer()
        if (::longBreakTimer.isInitialized) longBreakTimer.pauseTimer()
        mobileAlarm.cancel()
        mobileAlarm.stopLiveNotification()
        initTimers()
        _viewState.update { it.copy(timerState = TimerState.Pomodoro) }
        stopTimerUseCase()
    }

    fun onPageChanged(currentPage: Int) {
        _viewState.update { it.copy(selectedTabIndex = currentPage) }
    }

    fun onNavigatedToActivitiesScreen() {
        _viewState.update { it.copy(navigateToActivitiesScreen = false) }
    }

    private fun Long.formatToMMSS(): String {
        val time = LocalTime.fromMillisecondOfDay(this.toInt())
        return if (time.hour >= 1) {
            time.format(LocalTime.Format {
                hour()
                char(':')
                minute()
                char(':')
                second()
            })
        } else {
            time.format(LocalTime.Format {
                minute()
                char(':')
                second()
            })
        }
    }

    fun onResume() = viewModelScope.launch {
        resetKittidoroNumberUseCase()
        resetExercisesNumberUseCase()
    }

    fun onTabClick(clickedTabIndex: Int) {
        _tabClicks.tryEmit(clickedTabIndex)
    }
}