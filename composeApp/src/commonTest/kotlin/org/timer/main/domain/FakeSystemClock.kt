package org.timer.main.domain

import org.timer.main.domain.*

class FakeSystemClock() : SystemClock {

    var currentTime: Long = 0
    var currentTimeZone: String = "0"

    override fun now(): Long {
        return currentTime
    }

    override fun isToday(millis: Long): Boolean {
        return false
    }

    override fun getTimeZone(): String {
        return currentTimeZone
    }
}