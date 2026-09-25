package org.timer.main.domain.game

import kotlinx.coroutines.flow.*

interface AddFishWhenKittyFullUseCase {
    suspend operator fun invoke()
}

class DefaultAddFishWhenKittyFullUseCase(
    private val gameGateway: GameGateway,
) : AddFishWhenKittyFullUseCase {

    override suspend fun invoke() {
        val progress = gameGateway.getBowlFillingProgress().first()
        if (progress >= GameGateway.TOTAL_PROGRESS) {
            var fishNumber = gameGateway.getFishNumber().first()
            fishNumber++
            gameGateway.setFishNumber(fishNumber)
        }
    }
}