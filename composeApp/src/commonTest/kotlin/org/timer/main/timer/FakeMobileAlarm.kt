package org.timer.main.timer

import org.timer.main.domain.settings.*

class FakeMobileAlarm : MobileAlarm {
    var cancelCount = 0
    var scheduleCount = 0
    var startLiveNotificationCount = 0
    var stopLiveNotificationCount = 0
    var lastStartLiveNotificationTitle: String? = null
    var lastStartLiveNotificationIsBreak: Boolean? = null

    override fun cancel() {
        cancelCount++
    }

    override fun schedule(
        scheduledDateTimeMillis: Long,
        alarmSound: AlarmSound,
        title: String,
        body: String
    ) {
        scheduleCount++
    }

    override fun startLiveNotification(
        title: String,
        isBreak: Boolean,
        totalTimeLeftMillis: Long,
        alarmSound: AlarmSound
    ) {
        startLiveNotificationCount++
        lastStartLiveNotificationTitle = title
        lastStartLiveNotificationIsBreak = isBreak
    }

    override fun stopLiveNotification() {
        stopLiveNotificationCount++
    }
}
