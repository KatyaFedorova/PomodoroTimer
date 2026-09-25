package org.timer.main.domain.settings

import kotlinx.coroutines.flow.*

class FakeSettingsGateway : SettingsGateway {
    val timeSettingsFlow = MutableStateFlow(TimeSettings(0, 0, 0, 0))
    val alarmSoundFlow = MutableStateFlow(AlarmSound.CAT)
    private val hintSeenFlow = MutableStateFlow(false)

    var lastSetTimeSettings: TimeSettings? = null
    var lastSetAlarmSound: AlarmSound? = null

    override fun getTimeSettings(): Flow<TimeSettings> = timeSettingsFlow

    override suspend fun setTimeSettings(timeSettings: TimeSettings) {
        lastSetTimeSettings = timeSettings
        timeSettingsFlow.value = timeSettings
    }

    override suspend fun setAlarmSound(alarmSound: AlarmSound) {
        lastSetAlarmSound = alarmSound
        alarmSoundFlow.value = alarmSound
    }

    override fun getAlarmSound(): Flow<AlarmSound> = alarmSoundFlow

    override fun getSettingsHintSeen(): Flow<Boolean> = hintSeenFlow

    override suspend fun setSettingsHintSeen(seen: Boolean) {
        hintSeenFlow.value = seen
    }
}
