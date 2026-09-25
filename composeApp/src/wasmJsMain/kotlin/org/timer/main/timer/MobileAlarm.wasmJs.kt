package org.timer.main.timer

import org.timer.main.domain.settings.*

actual class DefaultMobileAlarm actual constructor(actual val context: Any?) :MobileAlarm{
    actual override fun  cancel() {}

    actual override fun schedule(
        scheduledDateTimeMillis: Long,
        alarmSound: AlarmSound,
        title: String,
        body: String
    ) {}

    actual override fun stopLiveNotification() {}
    actual override fun startLiveNotification(
        title: String,
        isBreak: Boolean,
        totalTimeLeftMillis: Long,
        alarmSound: AlarmSound
    ) {
    }

}