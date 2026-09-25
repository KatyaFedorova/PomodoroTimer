package org.timer.main.domain.settings

import kotlinx.coroutines.flow.*

interface SettingsGateway {
    fun getTimeSettings(): Flow<TimeSettings>
    suspend fun setTimeSettings(timeSettings: TimeSettings)
    suspend fun setAlarmSound(alarmSound: AlarmSound)
    fun getAlarmSound(): Flow<AlarmSound>
    fun getSettingsHintSeen(): Flow<Boolean>
    suspend fun setSettingsHintSeen(seen: Boolean)
}