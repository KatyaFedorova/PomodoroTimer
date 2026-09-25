package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface StatisticsEventGateway {
    suspend fun saveEvent(event: Event)
     fun getEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<Event>>
}