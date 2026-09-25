package org.timer.main.timer

import org.timer.main.domain.settings.*

expect class DefaultMobileAlarm(context: Any? = null) : MobileAlarm {
    val context: Any?
    override fun cancel()
    override fun schedule(
        scheduledDateTimeMillis: Long,
        alarmSound: AlarmSound,
        title: String,
        body: String
    )

    override fun startLiveNotification(
        title: String,
        isBreak: Boolean,
        totalTimeLeftMillis: Long,
        alarmSound: AlarmSound
    )

    override fun stopLiveNotification()

}

