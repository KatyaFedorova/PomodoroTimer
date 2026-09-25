package org.timer.main.domain.exercise

import kotlinx.coroutines.flow.Flow

interface ExerciseGateway {
    fun getProgress(): Flow<ExerciseProgress>
    fun getProgressByUserId(userId: String): Flow<ExerciseProgress>
    suspend fun updateProgress(progress: ExerciseProgress)
}