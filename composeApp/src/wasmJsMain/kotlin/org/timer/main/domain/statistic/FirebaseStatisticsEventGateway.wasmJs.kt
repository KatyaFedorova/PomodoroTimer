package org.timer.main.domain.statistic

import com.diamondedge.logging.*
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.js.Promise

// JS interop
external interface JsResult : kotlin.js.JsAny {
    val success: Boolean
    val error: String?
}

@JsName("myAppJsFirebase")
external object MyAppJsFirebase {
    fun saveEvent(eventJson: String): Promise<JsResult>
    fun listenToEventsInRange(fromMillis: Long, toMillis: Long, callback: (String) -> Unit): String
    fun removeEventsListener(listenerId: String)
}

actual class FirebaseStatisticsEventGateway : StatisticsEventGateway {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveEvent(event: Event) {
        val eventJson = json.encodeToString(event.toFirestoreEvent())
        val result: JsResult = MyAppJsFirebase.saveEvent(eventJson).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to save event")
        }
    }

    override fun getEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<Event>> = callbackFlow {
        val listenerId = MyAppJsFirebase.listenToEventsInRange(fromMillis, toMillis) { rawJson ->
            logging().d{"[FirebaseStatisticsEventGateway] rawJson=" + rawJson}
            try {
                if (rawJson.isBlank() || rawJson == "[]" || rawJson == "null") {
                    trySend(emptyList())
                } else {
                    val events: List<EventFirestore> = json.decodeFromString(rawJson)
                    trySend(events.map { it.toDomainEvent() })
                }
            } catch (e: Throwable) {
                logging().d{"[FirebaseStatisticsEventGateway] decode error. Raw: $rawJson" + e.message}
            }
        }

        awaitClose { MyAppJsFirebase.removeEventsListener(listenerId) }
    }
}

@Serializable
data class EventFirestore(
    val eventType: String,
    val meta: Map<String, JsonElement>,
    val timeZone: String,
    val timestamp: Long? = null
)

// Маппинг в domain Event
fun EventFirestore.toDomainEvent(): Event =
    Event(
        eventType = EventType.valueOf(eventType),
        meta = meta.mapValues { (_, v) ->
            when {
                v is JsonPrimitive && v.isString -> v.content
                v is JsonPrimitive && v.booleanOrNull != null -> v.boolean
                v is JsonPrimitive && v.longOrNull != null -> v.long
                v is JsonPrimitive && v.doubleOrNull != null -> v.double
                else -> v.toString()
            }
        },
        timeZone = timeZone,
        timestamp = timestamp
    )

fun Event.toFirestoreEvent(): EventFirestore =
    EventFirestore(
        eventType = this.eventType.name,
        meta = this.meta.mapValues { (_, v) ->
            when (v) {
                is String -> JsonPrimitive(v)
                is Int -> JsonPrimitive(v)
                is Long -> JsonPrimitive(v)
                is Double -> JsonPrimitive(v)
                is Boolean -> JsonPrimitive(v)
                else -> JsonPrimitive(v.toString())
            }
        },
        timeZone = this.timeZone,
        timestamp = this.timestamp
    )
