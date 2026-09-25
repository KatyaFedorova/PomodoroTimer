package org.timer.main.timer

data class TimerViewState(
    val pomodoroTime: String = "00:00",
    val shortBreakTime: String = "00:00",
    val longBreakTime: String = "00:00",
    val isPomodoroTimerRunning: Boolean = false,
    val isShortBreakTimerRunning: Boolean = false,
    val isLongBreakTimerRunning: Boolean = false,
    val kittyDoroNumber: Int = 0,
    val timerState: TimerState = TimerState.Pomodoro,
    val selectedTabIndex: Int = 0,
    val navigateToActivitiesScreen: Boolean = false,
) {
    val isPomodoroStartVisible: Boolean
        get() = !isPomodoroTimerRunning
    val isPomodoroPauseVisible: Boolean
        get() = isPomodoroTimerRunning

    val isShortBreakStartVisible: Boolean
        get() = !isShortBreakTimerRunning
    val isShortBreakPauseVisible: Boolean
        get() = isShortBreakTimerRunning

    val isLongBreakStartVisible: Boolean
        get() = !isLongBreakTimerRunning
    val isLongBreakPauseVisible: Boolean
        get() = isLongBreakTimerRunning

}

sealed class TimerState {
    data object Pomodoro : TimerState()
    data object ShortBreak : TimerState()
    data object LongBreak : TimerState()
}
