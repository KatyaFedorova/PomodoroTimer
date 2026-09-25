package org.timer.main.domain

import kotlinx.datetime.*

interface SystemClock {
    fun now(): Long
    fun isToday(millis: Long): Boolean
    fun getTimeZone(): String
}

class DefaultSystemClock : SystemClock {
    override fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    override fun isToday(millis: Long): Boolean {
        val date = Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return date == today
    }

    override  fun getTimeZone(): String {
        return platformTimeZoneId()
    }
}

expect fun platformTimeZoneId(): String
