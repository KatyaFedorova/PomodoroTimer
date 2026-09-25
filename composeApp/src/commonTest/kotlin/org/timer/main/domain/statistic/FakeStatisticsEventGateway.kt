package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

class FakeStatisticsEventGateway : StatisticsEventGateway {
    var savedEvent: Event? = null

    override suspend fun saveEvent(event: Event) {
        savedEvent = event
    }

    var eventsInRange: List<Event> = emptyList()
    override fun getEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<Event>> {
        return flow { emit(eventsInRange) }
    }
}