package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*
import org.timer.main.domain.*

interface ResetKittidoroNumberUseCase {
    suspend operator fun invoke()
}

class DefaultResetKittidoroNumberUseCase(
    private val timerGateway: TimerGateway,
    private val systemClock: SystemClock
) : ResetKittidoroNumberUseCase {

    override suspend fun invoke() {
        val timer = timerGateway.getTimer().first()
        if (!systemClock.isToday(timer.lastKittidorosUpdateTime)) {
            timer.kittidorosNumber = 0
            timer.lastKittidorosUpdateTime = systemClock.now()
            timerGateway.updateTimer(timer)
        }

    }
}