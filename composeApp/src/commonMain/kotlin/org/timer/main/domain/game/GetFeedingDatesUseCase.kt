package org.timer.main.domain.game

import kotlinx.coroutines.flow.*

interface GetFeedingDatesUseCase {
     operator fun invoke(): Flow<List<Long>>
}

class DefaultGetFeedingDatesUseCase(
    private val gateway: GameGateway
) : GetFeedingDatesUseCase {

    override fun invoke() = gateway.getKittyFeedDates()
}