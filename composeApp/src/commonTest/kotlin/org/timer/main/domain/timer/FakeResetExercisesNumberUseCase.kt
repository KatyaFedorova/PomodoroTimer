package org.timer.main.domain.timer

class FakeResetExercisesNumberUseCase : ResetExercisesNumberUseCase {
    var invokeCount = 0

    override suspend fun invoke() {
        invokeCount++
    }
}
