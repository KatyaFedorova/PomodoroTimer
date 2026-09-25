package org.timer.main.domain.exercise

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

actual class FirebaseExerciseGateway actual constructor() : ExerciseGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")

    private val firestore = Firebase.firestore

    private fun docFor(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("exercise")
            .document("progress")

    override fun getProgress(): Flow<ExerciseProgress> =
        getProgressByUserId(userId)

    override fun getProgressByUserId(userId: String): Flow<ExerciseProgress> {
        return docFor(userId).snapshots.map { snap ->
            if (!snap.exists) ExerciseProgress()
            else snap.data<ExerciseProgressDto>().toDomain()
        }
    }

    override suspend fun updateProgress(progress: ExerciseProgress) {
        val dto = progress.toDto()
        docFor(userId).set(dto, merge = true)
    }
}

@Serializable
private data class ExerciseProgressDto(
    val exercisesCount: Long = 0,
    val lastExercisesUpdateTime: Long = 0
)

private fun ExerciseProgressDto.toDomain(): ExerciseProgress =
    ExerciseProgress(
        exercisesCount = exercisesCount.toInt(),
        lastExercisesUpdateTime = lastExercisesUpdateTime
    )

private fun ExerciseProgress.toDto(): ExerciseProgressDto =
    ExerciseProgressDto(
        exercisesCount = exercisesCount.toLong(),
        lastExercisesUpdateTime = lastExercisesUpdateTime
    )