package org.timer.main.domain.game // Ensure this matches your project structure

import kotlinx.coroutines.await // For Promise.await()
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.js.Promise

external interface JsResult : JsAny {
    val success: Boolean
    val error: String?
}

@JsName("myAppJsFirebase")
private external object MyAppJsFirebaseGameBridge { // Using a more specific name to avoid clashes if MyAppJsFirebase is large
    fun listenToBowlProgress(callback: (progress: Int) -> Unit): String // JS sends Int directly
    fun removeBowlProgressListener(listenerId: String)
    fun setBowlProgress(progressValue: Int): Promise<JsResult>
    fun resetBowlProgress(): Promise<JsResult>
    fun listenToKittyFeedDates(callback: (datesJson: String) -> Unit): String
    fun removeKittyFeedDatesListener(listenerId: String)
    fun addKittyFeedDate(date: Double): Promise<JsResult>
    fun listenToLastResetTime(callback: (Double) -> Unit): String
    fun removeLastResetTimeListener(listenerId: String)
    fun updateLastResetTime(now: Double): Promise<JsResult>
    fun listenToFishNumber(callback: (progress: Int) -> Unit): String
    fun removeFishNumberProgressListener(listenerId: String)
    fun setFishNumber(progressValue: Int): Promise<JsResult>
}

actual class FirebaseGameGateway : GameGateway {

     override fun getBowlFillingProgress(): Flow<Int> = callbackFlow {
        var listenerId: String? = null
        try {
            listenerId = MyAppJsFirebaseGameBridge.listenToBowlProgress { progressValue ->
                trySend(progressValue)
            }
        } catch (e: Exception) {
            close(RuntimeException("Failed to initialize bowl progress listener: ${e.message}", e))
        }

        awaitClose {
            listenerId?.let {
                MyAppJsFirebaseGameBridge.removeBowlProgressListener(it)
            }
        }
    }

     override suspend fun setProgress(i: Int) {
        val result: JsResult = MyAppJsFirebaseGameBridge.setBowlProgress(i).await()
        if (!result.success) {
            throw Exception(result.error ?: "Unknown error setting bowl progress via JS bridge.")
        }
    }

     override suspend fun resetProgress() {
        val result: JsResult = MyAppJsFirebaseGameBridge.resetBowlProgress().await()
        if (!result.success) {
            throw Exception(result.error ?: "Unknown error resetting bowl progress via JS bridge.")
        }
    }

    override fun getKittyFeedDates() = callbackFlow {
        var listenerId: String? = null
        try {
            listenerId = MyAppJsFirebaseGameBridge.listenToKittyFeedDates { jsonString ->
                val dates: List<Long> = try {
                    kotlinx.serialization.json.Json.decodeFromString(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(dates)
            }
        } catch (e: Exception) {
            close(RuntimeException("Failed to initialize kitty feed dates listener: ${e.message}", e))
        }

        awaitClose {
            listenerId?.let {
                MyAppJsFirebaseGameBridge.removeKittyFeedDatesListener(it)
            }
        }
    }

    override suspend fun saveKittyFeedDate(date: Long) {
        val result: JsResult = MyAppJsFirebaseGameBridge.addKittyFeedDate(date.toDouble()).await()
        if (!result.success) {
            throw Exception(result.error ?: "Unknown error adding kitty feed date via JS bridge.")
        }
    }

    override fun getLastResetTime(): Flow<Long> = callbackFlow {
        var listenerId: String? = null
        try {
            listenerId = MyAppJsFirebaseGameBridge.listenToLastResetTime { value ->
                trySend(value.toLong())
            }
        } catch (e: Exception) {
            close(RuntimeException("Failed to initialize lastResetTime listener: ${e.message}", e))
        }

        awaitClose {
            listenerId?.let {
                MyAppJsFirebaseGameBridge.removeLastResetTimeListener(it)
            }
        }
    }

    override suspend fun updateLastResetTime(now: Long) {
        val result: JsResult = MyAppJsFirebaseGameBridge.updateLastResetTime(now.toDouble()).await()
        if (!result.success) {
            throw Exception(result.error ?: "Unknown error updating lastResetTime via JS bridge.")
        }
    }

    override fun getFishNumber(): Flow<Int> = callbackFlow {
        var listenerId: String? = null
        try {
            listenerId = MyAppJsFirebaseGameBridge.listenToFishNumber { progressValue ->
                trySend(progressValue)
            }
        } catch (e: Exception) {
            close(RuntimeException("Failed to initialize fish number listener: ${e.message}", e))
        }

        awaitClose {
            listenerId?.let {
                MyAppJsFirebaseGameBridge.removeFishNumberProgressListener(it)
            }
        }
    }

    override suspend fun setFishNumber(fishNumber: Int) {
        val result: JsResult = MyAppJsFirebaseGameBridge.setFishNumber(fishNumber).await()
        if (!result.success) {
            throw Exception(result.error ?: "Unknown error setting fish number via JS bridge.")
        }
    }
}
