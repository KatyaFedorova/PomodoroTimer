package org.timer.main.timer

import org.timer.main.domain.settings.*

interface AlarmPlayer {
    fun play(alarmSound: AlarmSound, onEnded: () -> Unit = {})
    fun cancel()
}

expect class DefaultAlarmPlayer(context: Any? = null) : AlarmPlayer {
    val context: Any?
    override fun play(alarmSound: AlarmSound, onEnded: () -> Unit)
    override fun cancel()
}
