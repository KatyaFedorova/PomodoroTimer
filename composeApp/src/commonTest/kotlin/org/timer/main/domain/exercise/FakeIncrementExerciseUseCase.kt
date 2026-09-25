package org.timer.main.domain.exercise

class FakeIncrementExerciseUseCase : IncrementExerciseUseCase {
    var invokeCount = 0

    override suspend fun invoke() {
        invokeCount++
    }
}
