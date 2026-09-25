package org.timer.main.timer

import org.timer.main.domain.settings.*

interface MobileAlarm {
    fun cancel()
    fun schedule(
        scheduledDateTimeMillis: Long,
        alarmSound: AlarmSound,
        title: String,
        body: String
    )

    fun startLiveNotification(
        title: String,
        isBreak: Boolean,
        totalTimeLeftMillis: Long,
        alarmSound: AlarmSound
    )

    fun stopLiveNotification()
}




