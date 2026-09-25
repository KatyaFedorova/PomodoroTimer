package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*
import org.timer.main.domain.settings.*
import org.timer.main.timer.*

interface PlayAlarmUseCase {
    suspend fun invoke(onEnded: () -> Unit = {})
}

class DefaultPlayAlarmUseCase(
    private val alarmPlayer: AlarmPlayer,
    private val settingsGateway: SettingsGateway
) : PlayAlarmUseCase {

    override suspend fun invoke(onEnded: () -> Unit) {
        val alarmSound = settingsGateway.getAlarmSound().first()
        alarmPlayer.play(alarmSound, onEnded = onEnded)
    }
}