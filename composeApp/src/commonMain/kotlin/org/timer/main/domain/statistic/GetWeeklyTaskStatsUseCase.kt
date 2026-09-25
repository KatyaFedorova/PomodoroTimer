package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetWeeklyTaskStatsUseCase {
    operator fun invoke(weekOffset: Int): Flow<WeeklyEventStats>
}

class DefaultGetWeeklyTaskStatsUseCase(
    private val delegate: GetWeeklyEventStatsUseCase
) : GetWeeklyTaskStatsUseCase {
    override fun invoke(weekOffset: Int) =
        delegate(weekOffset, listOf(EventType.TASK_COMPLETED))
}