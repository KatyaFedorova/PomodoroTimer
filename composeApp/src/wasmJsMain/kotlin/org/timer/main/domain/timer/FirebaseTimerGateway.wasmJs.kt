package org.timer.main.domain.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.js.Promise

// ---------- JS Result ----------
external interface JsResult : JsAny {
    val success: Boolean
    val error: String?
}

// ---------- JS Bridge ----------
@JsName("myAppJsFirebase")
external object MyAppJsFirebaseTimer {
    fun listenToTimer(callback: (String?) -> Unit): String
    fun removeTimerListener(listenerId: String)
    fun setTimer(timerJson: String): Promise<JsResult>

    fun listenToTimerByUserId(userId: String, callback: (String?) -> Unit): String
    fun removeTimerByUserIdListener(listenerId: String)

}

// ---------- JSON config ----------
private val json = Json {
    encodeDefaults = true     // ✅ пишем все поля, даже если дефолтные
    ignoreUnknownKeys = true
}

actual class FirebaseTimerGateway : TimerGateway {

    override fun getTimer(): Flow<Timer> = callbackFlow {
        val listenerId = MyAppJsFirebaseTimer.listenToTimer { jsonStr ->
            if (jsonStr == null) {
                trySend(Timer(0, 0))
            } else {
                try {
                    val dto = json.decodeFromString<TimerDto>(jsonStr)
                    trySend(dto.toDomain())
                } catch (e: Throwable) {
                    close(e)
                }
            }
        }
        awaitClose { MyAppJsFirebaseTimer.removeTimerListener(listenerId) }
    }

    override fun getTimerByUserId(userId: String): Flow<Timer> = callbackFlow {
        val listenerId = MyAppJsFirebaseTimer.listenToTimerByUserId(userId) { jsonStr ->
            if (jsonStr == null) {
                trySend(Timer(0, 0))
            } else {
                try {
                    val dto = json.decodeFromString<TimerDto>(jsonStr)
                    trySend(dto.toDomain())
                } catch (e: Throwable) {
                    close(e)
                }
            }
        }
        awaitClose { MyAppJsFirebaseTimer.removeTimerByUserIdListener(listenerId) }
    }

    override suspend fun updateTimer(timer: Timer) {
        val dto = timer.toDto()
        val jsonStr = json.encodeToString(dto)
        val result: JsResult = MyAppJsFirebaseTimer.setTimer(jsonStr).await()
        if (!result.success) throw Exception("Failed to updateTimer: ${result.error}")
    }
}


@Serializable
data class TimerDto(
    val kittidorosNumber: Int = 0,
    val lastKittidorosUpdateTime: Long = 0,
    val mode: TimerMode = TimerMode.NONE, // 👈 добавляем
    val durationMillis: Long? = null,
    val startAt: Long? = null
)

fun TimerDto.toDomain(): Timer = Timer(
    kittidorosNumber = kittidorosNumber,
    lastKittidorosUpdateTime = lastKittidorosUpdateTime,
    mode = mode,
    durationMillis = durationMillis,
    startAt = startAt
)

fun Timer.toDto(): TimerDto = TimerDto(
    kittidorosNumber = kittidorosNumber,
    lastKittidorosUpdateTime = lastKittidorosUpdateTime,
    mode = mode,
    durationMillis = durationMillis,
    startAt = startAt
)