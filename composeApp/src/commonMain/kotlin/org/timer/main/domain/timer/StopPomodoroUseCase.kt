package org.timer.main.domain.timer

import kotlinx.coroutines.flow.first

interface StopTimerUseCase {
    suspend operator fun invoke()
}

class DefaultStopTimerUseCase(
    private val timerGateway: TimerGateway
) : StopTimerUseCase {
    override suspend operator fun invoke() {
        val timer = timerGateway.getTimer().first()

        val updated = timer.copy(
            mode = TimerMode.NONE,
            durationMillis = 0L,
            startAt = null
        )

        timerGateway.updateTimer(updated)
    }
}
