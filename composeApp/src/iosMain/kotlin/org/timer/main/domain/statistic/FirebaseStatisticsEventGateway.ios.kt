package org.timer.main.domain.statistic

import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.*

actual class FirebaseStatisticsEventGateway : StatisticsEventGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")

    private val firestore = Firebase.firestore

    override suspend fun saveEvent(event: Event) {
        val doc = mapOf(
            "userId" to userId,
            "eventType" to event.eventType.name,
            "meta" to event.meta,
            "timeZone" to event.timeZone,
            "timestamp" to FieldValue.serverTimestamp
        )
        firestore.collection("events").add(doc)
    }

    override fun getEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<Event>> {
        return firestore.collection("events")
            .where {
                "userId" equalTo userId and
                        ("timestamp" greaterThanOrEqualTo millisToTimestamp(fromMillis)) and
                        ("timestamp" lessThan millisToTimestamp(toMillis))
            }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { doc ->
                    val eventTypeStr = doc.get<String>("eventType")
                    val eventType = runCatching { EventType.valueOf(eventTypeStr) }.getOrNull()
                        ?: return@mapNotNull null

                    val ts = doc.get<Timestamp?>("timestamp")?.toMillis()

                    Event(
                        eventType = eventType,
                        meta = emptyMap(),
                        timestamp = ts,
                        timeZone = doc.get<String>("timeZone")
                    )
                }
            }
    }

    private fun millisToTimestamp(millis: Long): Timestamp {
        val secs = millis / 1000
        val nanos = ((millis % 1000) * 1_000_000).toInt()
        return Timestamp(seconds = secs, nanoseconds = nanos)
    }

    private fun Timestamp.toMillis(): Long =
        this.seconds * 1000 + this.nanoseconds / 1_000_000

}
