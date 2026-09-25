package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetWeeklyPomodoroStatsUseCase {
    operator fun invoke(weekOffset: Int): Flow<WeeklyEventStats>
}

class DefaultGetWeeklyPomodoroStatsUseCase(
    private val delegate: GetWeeklyEventStatsUseCase
) : GetWeeklyPomodoroStatsUseCase {
    override fun invoke(weekOffset: Int) =
        delegate(weekOffset, listOf(EventType.POMODORO_COMPLETED))
}