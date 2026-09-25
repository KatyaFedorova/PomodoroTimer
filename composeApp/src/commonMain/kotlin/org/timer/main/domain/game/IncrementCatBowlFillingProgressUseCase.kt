package org.timer.main.domain.game

import kotlinx.coroutines.flow.*
import org.timer.main.domain.*

interface IncrementCatBowlFillingProgressUseCase {
    suspend operator fun invoke()
}

class DefaultIncrementCatBowlFillingProgressUseCase(
    private val gameGateway: GameGateway,
    private val clock: SystemClock
) : IncrementCatBowlFillingProgressUseCase {

    override suspend fun invoke() {
        var progress = gameGateway.getBowlFillingProgress().first()
        progress++
        when {
            progress < GameGateway.TOTAL_PROGRESS -> {
                gameGateway.setProgress(progress)
            }
            progress == GameGateway.TOTAL_PROGRESS -> {
                gameGateway.setProgress(progress)
                gameGateway.saveKittyFeedDate(clock.now())
            }
        }
    }
}