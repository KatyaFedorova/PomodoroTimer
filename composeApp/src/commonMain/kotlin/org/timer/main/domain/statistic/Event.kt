package org.timer.main.domain.statistic

data class Event(
    val eventType: EventType,
    val meta: Map<String, Any>,
    val timestamp: Long? = null,
    val timeZone: String
)