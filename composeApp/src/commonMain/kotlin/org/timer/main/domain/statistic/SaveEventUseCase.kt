package org.timer.main.domain.statistic

import org.timer.main.domain.*

interface SaveEventUseCase {
    suspend operator fun invoke(type: EventType, meta: Map<String, Any> = emptyMap())
}

class DefaultSaveEventUseCase(
    private val statisticsEventRepository: StatisticsEventGateway,
    private val systemClock: SystemClock
) : SaveEventUseCase {

    override suspend fun invoke(type: EventType, meta: Map<String, Any>) {
        val event = Event(type, meta, null, systemClock.getTimeZone())
        statisticsEventRepository.saveEvent(event)
    }
}