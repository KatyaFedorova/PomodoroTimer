package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*
import org.timer.main.domain.*
import org.timer.main.domain.exercise.*

interface ResetExercisesNumberUseCase {
    suspend operator fun invoke()
}

class DefaultResetExercisesNumberUseCase(
    private val exerciseGateway: ExerciseGateway,
    private val systemClock: SystemClock
) : ResetExercisesNumberUseCase {

    override suspend fun invoke() {
        val exerciseProgress = exerciseGateway.getProgress().first()
        if (!systemClock.isToday(exerciseProgress.lastExercisesUpdateTime)) {
            exerciseProgress.exercisesCount = 0
            exerciseProgress.lastExercisesUpdateTime = systemClock.now()
            exerciseGateway.updateProgress(exerciseProgress)
        }

    }
}