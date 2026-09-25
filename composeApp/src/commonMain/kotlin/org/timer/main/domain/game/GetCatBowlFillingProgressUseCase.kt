package org.timer.main.domain.game

import kotlinx.coroutines.flow.*

interface GetCatBowlFillingProgressUseCase {
     operator fun invoke(): Flow<Int>
}

class DefaultGetCatBowlFillingProgressUseCase(
    private val gameGateway: GameGateway
) : GetCatBowlFillingProgressUseCase {

    override  fun invoke(): Flow<Int> = gameGateway.getBowlFillingProgress()
}