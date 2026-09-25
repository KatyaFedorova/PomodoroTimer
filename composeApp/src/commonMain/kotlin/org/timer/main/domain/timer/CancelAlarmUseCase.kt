package org.timer.main.domain.timer

import org.timer.main.timer.*

interface CancelAlarmUseCase {
    fun invoke()
}

class DefaultCancelAlarmUseCase(
    private val alarmPlayer: AlarmPlayer,
) : CancelAlarmUseCase {
     override fun invoke() {
        alarmPlayer.cancel()
    }
}