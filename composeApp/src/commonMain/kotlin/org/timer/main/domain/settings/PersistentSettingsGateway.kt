package org.timer.main.domain.settings

import com.russhwolf.settings.*
import kotlinx.coroutines.flow.*



class PersistentSettingsGateway : SettingsGateway {
    private val settings: Settings = Settings()

    private val timeSettingsFlow =
        MutableStateFlow(TimeSettings(0,25000L * 60, 5000L * 60, 15000L * 60))

    private val alarmSoundFlow = MutableStateFlow(AlarmSound.entries[settings.getInt(KEY, 0)])


    override fun getTimeSettings(): Flow<TimeSettings> = timeSettingsFlow.asStateFlow()

    override suspend fun setTimeSettings(timeSettings: TimeSettings) {
        timeSettingsFlow.value = timeSettings
    }

    override suspend fun setAlarmSound(alarmSound: AlarmSound) {
        alarmSoundFlow.value = alarmSound
        settings.putInt(KEY, alarmSound.ordinal)
    }

    override fun getAlarmSound() = alarmSoundFlow.asStateFlow()
    override fun getSettingsHintSeen(): Flow<Boolean> {
        return flow { emit(true) }
    }

    override suspend fun setSettingsHintSeen(seen: Boolean) {

    }

    private companion object {
        private const val KEY = "alarmSound"
    }
}