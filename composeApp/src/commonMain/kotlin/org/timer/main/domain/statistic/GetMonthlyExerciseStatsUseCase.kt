package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetMonthlyExerciseStatsUseCase {
    operator fun invoke(monthOffset: Int): Flow<MonthlyEventStats>
}

class DefaultGetMonthlyExerciseStatsUseCase(
    private val delegate: GetMonthlyEventStatsUseCase
) : GetMonthlyExerciseStatsUseCase {
    override fun invoke(monthOffset: Int) =
        delegate(
            monthOffset,
            listOf(
                EventType.EXERCISE_HIGH_ENERGY_COMPLETED,
                EventType.EXERCISE_LOW_ENERGY_COMPLETED
            )
        )
}