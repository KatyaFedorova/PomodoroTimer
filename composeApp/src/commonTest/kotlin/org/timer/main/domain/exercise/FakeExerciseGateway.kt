package org.timer.main.domain.exercise

import kotlinx.coroutines.flow.*

class FakeExerciseGateway : ExerciseGateway {
    val progressFlow = MutableStateFlow(ExerciseProgress())
    private val byUser = mutableMapOf<String, MutableStateFlow<ExerciseProgress>>()

    override fun getProgress(): Flow<ExerciseProgress> = progressFlow

    override fun getProgressByUserId(userId: String): Flow<ExerciseProgress> =
        byUser.getOrPut(userId) { MutableStateFlow(ExerciseProgress()) }

    override suspend fun updateProgress(progress: ExerciseProgress) {
        progressFlow.value = progress
    }

    fun setProgressForUser(userId: String, progress: ExerciseProgress) {
        byUser.getOrPut(userId) { MutableStateFlow(ExerciseProgress()) }.value = progress
    }
}
