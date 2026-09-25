package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetMonthlyTaskStatsUseCase {
    operator fun invoke(monthOffset: Int): Flow<MonthlyEventStats>
}

class DefaultGetMonthlyTaskStatsUseCase(
    private val delegate: GetMonthlyEventStatsUseCase
) : GetMonthlyTaskStatsUseCase {
    override fun invoke(monthOffset: Int) =
        delegate(monthOffset, listOf(EventType.TASK_COMPLETED))
}