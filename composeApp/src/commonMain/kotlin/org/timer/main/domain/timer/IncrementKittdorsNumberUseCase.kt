package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*
import org.timer.main.domain.*
import org.timer.main.domain.statistic.*

interface IncrementKittdorsNumberUseCase {
    suspend operator fun invoke()
}

class DefaultIncrementKittdorsNumberUseCase(
    private val timerGateway: TimerGateway,
    private val clock: SystemClock,
    private val saveEventUseCase: SaveEventUseCase
) : IncrementKittdorsNumberUseCase {

    override suspend fun invoke() {
        val timer = timerGateway.getTimer().first()
        timer.kittidorosNumber += 1
        timer.lastKittidorosUpdateTime = clock.now()
        timerGateway.updateTimer(timer)
        saveEventUseCase.invoke(EventType.POMODORO_COMPLETED)
    }
}