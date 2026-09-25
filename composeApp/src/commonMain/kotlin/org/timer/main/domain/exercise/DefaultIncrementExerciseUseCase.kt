// domain/exercise/IncrementExerciseUseCase.kt
package org.timer.main.domain.exercise

import kotlinx.coroutines.flow.first
import org.timer.main.domain.*

interface IncrementExerciseUseCase {
    suspend operator fun invoke()
}

class DefaultIncrementExerciseUseCase(
    private val gateway: ExerciseGateway,
    private val clock: SystemClock
) : IncrementExerciseUseCase {
    override suspend operator fun invoke() {
        var current = gateway.getProgress().first().exercisesCount
        current++
        gateway.updateProgress(
            ExerciseProgress(
                exercisesCount = current,
                lastExercisesUpdateTime = clock.now()
            )
        )
    }
}
