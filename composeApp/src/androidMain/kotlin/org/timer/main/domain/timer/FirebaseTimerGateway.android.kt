package org.timer.main.domain.timer

import com.diamondedge.logging.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

actual class FirebaseTimerGateway actual constructor() : TimerGateway {
    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")

    private val firestore = Firebase.firestore
    private val defaultTimeSettings = Timer(0, 0)


    override fun getTimer(): Flow<Timer> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(defaultTimeSettings)
                } else {
                    val doc = firestore.collection("users")
                        .document(userId)
                        .collection("timer")
                        .document("timer")

                    doc.snapshots.map { snap ->
                        if (!snap.exists) {
                            defaultTimeSettings
                        } else {
                            snap.data<TimerDto>().toDomain()
                        }
                    }
                }
            }
    }

    override fun getTimerByUserId(userId: String): Flow<Timer> {
        val doc = firestore.collection("users")
            .document(userId)
            .collection("timer")
            .document("timer")

        return doc.snapshots.map { snap ->
            if (!snap.exists) {
                defaultTimeSettings
            } else {
                snap.data<TimerDto>().toDomain()
            }
        }
    }

    override suspend fun updateTimer(timer: Timer) {
        Firebase.auth.authStateChanged.first() ?: return
        val doc = firestore.collection("users")
            .document(userId)
            .collection("timer")
            .document("timer")

        val data = mapOf(
            "kittidorosNumber" to timer.kittidorosNumber.toLong(),
            "lastKittidorosUpdateTime" to timer.lastKittidorosUpdateTime,
            "mode" to timer.mode.name,
            "durationMillis" to timer.durationMillis,
            "startAt" to when {
                timer.mode != TimerMode.NONE && timer.startAt == null -> FieldValue.serverTimestamp
                timer.mode == TimerMode.NONE -> null
                else -> timer.startAt?.let { Timestamp.fromMilliseconds(it.toDouble()) }
            }
        )

        doc.set(data, merge = true)
    }

}


@Serializable
data class TimerDto(
    val kittidorosNumber: Long = 0,
    val lastKittidorosUpdateTime: Long = 0,
    val mode: TimerMode = TimerMode.NONE, // 👈 вместо isPomodoroRunning
    val durationMillis: Long? = null,
    val startAt: Timestamp? = null // Firestore Timestamp
)


private fun TimerDto.toDomain(): Timer {
    return Timer(
        kittidorosNumber = kittidorosNumber.toInt(),
        lastKittidorosUpdateTime = lastKittidorosUpdateTime,
        mode = mode,
        durationMillis = durationMillis,
        startAt = startAt?.toMilliseconds()?.toLong()
    )
}
