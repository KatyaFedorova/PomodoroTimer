package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetMonthlyPomodoroStatsUseCase {
    operator fun invoke(monthOffset: Int): Flow<MonthlyEventStats>
}

class DefaultGetMonthlyPomodoroStatsUseCase(
    private val delegate: GetMonthlyEventStatsUseCase
) : GetMonthlyPomodoroStatsUseCase {
    override fun invoke(monthOffset: Int) =
        delegate(monthOffset, listOf(EventType.POMODORO_COMPLETED))
}