package org.timer.main.domain.game

import kotlinx.coroutines.flow.*

interface GetFishesNumberUseCaseUseCase {
     operator fun invoke(): Flow<Int>
}

class DefaultGetFishesNumberUseCase(
    private val gameGateway: GameGateway
) : GetFishesNumberUseCaseUseCase {

    override  fun invoke(): Flow<Int> = gameGateway.getFishNumber()
}