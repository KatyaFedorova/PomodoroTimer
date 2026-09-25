package org.timer.main.timer

import org.timer.main.domain.settings.*

class FakeAlarmPlayer : AlarmPlayer {
    var playCount = 0
    var cancelCount = 0
    var lastPlayedSound: AlarmSound? = null

    override fun play(alarmSound: AlarmSound, onEnded: () -> Unit) {
        playCount++
        lastPlayedSound = alarmSound
        onEnded()
    }

    override fun cancel() {
        cancelCount++
    }
}
