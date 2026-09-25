package org.timer.main.domain.timer

import kotlinx.coroutines.flow.first

interface StartTimerUseCase {
    suspend operator fun invoke(mode: TimerMode, durationMillis: Long)
}

class DefaultStartTimerUseCase(
    private val timerGateway: TimerGateway
) : StartTimerUseCase {
    override suspend operator fun invoke(mode: TimerMode, durationMillis: Long) {
        val timer = timerGateway.getTimer().first()

        val updated = timer.copy(
            mode = mode,
            durationMillis = durationMillis,
            startAt = null // gateway заменит на FieldValue.serverTimestamp
        )

        timerGateway.updateTimer(updated)
    }
}

class StartPomodoroUseCase(
    private val startTimer: StartTimerUseCase
) {
    suspend operator fun invoke(durationMillis: Long) =
        startTimer(TimerMode.POMODORO, durationMillis)
}

class StartShortBreakUseCase(
    private val startTimer: StartTimerUseCase
) {
    suspend operator fun invoke(durationMillis: Long) =
        startTimer(TimerMode.SHORT_BREAK, durationMillis)
}

class StartLongBreakUseCase(
    private val startTimer: StartTimerUseCase
) {
    suspend operator fun invoke(durationMillis: Long) =
        startTimer(TimerMode.LONG_BREAK, durationMillis)
}
