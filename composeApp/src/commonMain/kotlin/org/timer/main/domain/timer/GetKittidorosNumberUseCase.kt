package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*

interface GetKittidorosNumberUseCase {
     operator fun invoke(): Flow<Int>
}

class DefaultGetKittidorosNumberUseCase(
    private val timerGateway: TimerGateway
) : GetKittidorosNumberUseCase {

    override fun invoke(): Flow<Int> {
        return timerGateway.getTimer().map { it.kittidorosNumber }
    }
}