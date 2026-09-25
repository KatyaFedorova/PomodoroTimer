package org.timer.main.domain.game

import com.diamondedge.logging.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.*

actual class FirebaseGameGateway : GameGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated for GameGateway")

    private val gameProgressDocRef: DocumentReference
        get() = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("game")
            .document("bowlProgress")

    private val lastResetTimeRef: DocumentReference
        get() = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("game")
            .document("resetTime")

    private val currentFieldKey = "current"

    override fun getBowlFillingProgress(): Flow<Int> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(0)
                } else {
                    gameProgressDocRef
                        .snapshots
                        .map { documentSnapshot: DocumentSnapshot? ->
                            if (documentSnapshot?.exists == true) {
                                documentSnapshot.get<Int>(currentFieldKey)
                            } else 0
                        }
                }
            }
    }

    override suspend fun setProgress(i: Int) {
        gameProgressDocRef.set(mapOf(currentFieldKey to i))
    }

    override suspend fun resetProgress() {
        gameProgressDocRef.set(mapOf(currentFieldKey to 0))
    }

    override fun getKittyFeedDates(): Flow<List<Long>> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(emptyList())
                } else {
                    gameProgressDocRef
                        .collection("feedingDates")
                        .snapshots
                        .map { querySnapshot ->
                            if (querySnapshot.documents.isEmpty()) {
                                emptyList()
                            } else {
                                querySnapshot.documents.map { doc ->
                                    doc.get<Long>("timestamp")
                                }
                            }
                        }
                }
            }
    }

    override suspend fun saveKittyFeedDate(date: Long) {
        gameProgressDocRef
            .collection("feedingDates")
            .add(mapOf("timestamp" to date))
    }

    override fun getLastResetTime(): Flow<Long> {
        return lastResetTimeRef
            .snapshots
            .map { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot?.exists == true) {
                    documentSnapshot.get<Long>(currentFieldKey)
                } else 0L
            }
    }

    override suspend fun updateLastResetTime(now: Long) {
        lastResetTimeRef.set(mapOf(currentFieldKey to now))
    }

    private val gameFishNumberRef: DocumentReference
        get() = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("game")
            .document("fishNumber")

    override fun getFishNumber(): Flow<Int> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(0)
                } else {
                    gameFishNumberRef
                        .snapshots
                        .map { documentSnapshot: DocumentSnapshot? ->
                            if (documentSnapshot?.exists == true) {
                                documentSnapshot.get<Int>(currentFieldKey)
                            } else 0
                        }
                }
            }
    }

    override suspend fun setFishNumber(fishNumber: Int) {
        gameFishNumberRef.set(mapOf(currentFieldKey to fishNumber))
    }


    private companion object {
        val logger = logging("FirebaseGameGateway")
    }

}
