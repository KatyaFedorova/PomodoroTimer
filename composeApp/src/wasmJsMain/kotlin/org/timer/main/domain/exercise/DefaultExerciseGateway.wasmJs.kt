package org.timer.main.domain.exercise

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.js.Promise

// --- JS Bridge ---
external interface JsResult : JsAny {
    val success: Boolean
    val error: String?
}

@JsName("myAppJsFirebase")
external object MyAppJsExercise {
    fun listenToExerciseProgress(callback: (String?) -> Unit): String
    fun removeExerciseListener(listenerId: String)
    fun listenToExerciseProgressByUserId(userId: String, callback: (String?) -> Unit): String
    fun setExerciseProgress(json: String): Promise<JsResult>
}

// --- JSON config ---
private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

// --- Gateway impl ---
actual class FirebaseExerciseGateway : ExerciseGateway {

    override fun getProgress(): Flow<ExerciseProgress> = callbackFlow {
        val id = MyAppJsExercise.listenToExerciseProgress { s ->
            if (s == null) {
                trySend(ExerciseProgress())
            } else {
                try {
                    val dto = json.decodeFromString<ExerciseProgressDto>(s)
                    trySend(dto.toDomain())
                } catch (e: Throwable) {
                    close(e)
                }
            }
        }
        awaitClose { MyAppJsExercise.removeExerciseListener(id) }
    }

    override fun getProgressByUserId(userId: String): Flow<ExerciseProgress> = callbackFlow {
        val id = MyAppJsExercise.listenToExerciseProgressByUserId(userId) { s ->
            if (s == null) {
                trySend(ExerciseProgress())
            } else {
                try {
                    val dto = json.decodeFromString<ExerciseProgressDto>(s)
                    trySend(dto.toDomain())
                } catch (e: Throwable) {
                    close(e)
                }
            }
        }
        awaitClose { MyAppJsExercise.removeExerciseListener(id) }
    }

    override suspend fun updateProgress(progress: ExerciseProgress) {
        val dto = progress.toDto()
        val payload = json.encodeToString(dto)
        val r:JsResult = MyAppJsExercise.setExerciseProgress(payload).await()
        if (!r.success) throw IllegalStateException("setExerciseProgress failed: ${r.error}")
    }
}

// --- DTO ---
@Serializable
private data class ExerciseProgressDto(
    val exercisesCount: Int = 0,
    val lastExercisesUpdateTime: Long = 0
)

private fun ExerciseProgressDto.toDomain() =
    ExerciseProgress(exercisesCount, lastExercisesUpdateTime)

private fun ExerciseProgress.toDto() =
    ExerciseProgressDto(exercisesCount, lastExercisesUpdateTime)
