package org.timer.main.domain.settings

import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

actual class FirebaseSettingsGateway : SettingsGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")

    private val firestore = Firebase.firestore
    private val defaultTimeSettings = TimeSettings(
        0,
        pomodoroTime = 25000L * 60,
        shortBreakTime = 5000L * 60,
        longBreakTime = 15000L * 60
    )

    @OptIn(ExperimentalCoroutinesApi::class)
     override fun getTimeSettings(): Flow<TimeSettings> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(defaultTimeSettings)
                } else {
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("settings")
                        .document("timeSettings")
                        .snapshots
                        .map { snap ->
                            if (snap.exists){
                                snap.data<TimeSettings>()
                            } else defaultTimeSettings
                        }

                }
            }
    }

     override suspend fun setTimeSettings(timeSettings: TimeSettings) {
        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("timeSettings")
            .set(timeSettings)
    }


     override fun getAlarmSound(): Flow<AlarmSound> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(AlarmSound.STANDARD)
                } else {
                    firestore.collection("users")
                        .document(userId)
                        .collection("settings")
                        .document("alarmSound")
                        .snapshots
                        .map { snapshot ->
                            if (snapshot.exists) {
                                snapshot.data<Map<String, String>>()["sound"]?.let { name ->
                                    AlarmSound.valueOf(name)
                                } ?: AlarmSound.STANDARD
                            } else {
                                AlarmSound.STANDARD
                            }

                        }
                }
            }
    }

    override fun getSettingsHintSeen(): Flow<Boolean> {
        return flow { emit(true) }
    }

    override suspend fun setSettingsHintSeen(seen: Boolean) {

    }

    override suspend fun setAlarmSound(alarmSound: AlarmSound) {
        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("alarmSound").set(mapOf("sound" to alarmSound.name))
    }
}
