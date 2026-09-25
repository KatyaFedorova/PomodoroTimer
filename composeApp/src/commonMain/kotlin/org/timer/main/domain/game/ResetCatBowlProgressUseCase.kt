package org.timer.main.domain.game

import kotlinx.coroutines.flow.*
import org.timer.main.domain.*

interface ResetCatBowlFillingProgressUseCase {
    suspend operator fun invoke()
}

class DefaultResetCatBowlFillingProgressUseCase(
    private val gameGateway: GameGateway,
    private val systemClock: SystemClock
) : ResetCatBowlFillingProgressUseCase {

    override suspend fun invoke() {
        val lastResetTime = gameGateway.getLastResetTime().first()
        if (!systemClock.isToday(lastResetTime)){
            gameGateway.resetProgress()
            gameGateway.updateLastResetTime(systemClock.now())
        }

    }
}