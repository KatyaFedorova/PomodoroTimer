package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*

interface GetWeeklyExerciseStatsUseCase {
    operator fun invoke(weekOffset: Int): Flow<WeeklyEventStats>
}

class DefaultGetWeeklyExerciseStatsUseCase(
    private val delegate: GetWeeklyEventStatsUseCase
) : GetWeeklyExerciseStatsUseCase {
    override fun invoke(weekOffset: Int) =
        delegate(
            weekOffset,
            listOf(
                EventType.EXERCISE_HIGH_ENERGY_COMPLETED,
                EventType.EXERCISE_LOW_ENERGY_COMPLETED
            )
        )
}