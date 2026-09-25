package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

/** Records the arguments the per-category delegates forward to the shared event-stats use case. */
class FakeGetWeeklyEventStatsUseCase : GetWeeklyEventStatsUseCase {
    var lastWeekOffset: Int? = null
    var lastEventTypes: List<EventType>? = null

    override fun invoke(weekOffset: Int, eventTypes: List<EventType>): Flow<WeeklyEventStats> {
        lastWeekOffset = weekOffset
        lastEventTypes = eventTypes
        return emptyFlow()
    }
}

class FakeGetMonthlyEventStatsUseCase : GetMonthlyEventStatsUseCase {
    var lastMonthOffset: Int? = null
    var lastEventTypes: List<EventType>? = null

    override fun invoke(monthOffset: Int, eventTypes: List<EventType>): Flow<MonthlyEventStats> {
        lastMonthOffset = monthOffset
        lastEventTypes = eventTypes
        return emptyFlow()
    }
}
