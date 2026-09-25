package org.timer.main.settings

import org.timer.main.domain.settings.*

data class SettingsViewState(
    val selectedPresetPosition: Int = 0,
    val pomodoroMinutes: String = "",
    val shortBreakMinutes: String = "",
    val longBreakMinutes: String = "",
    val showPomodoroError: Boolean = false,
    val showShortBreakError: Boolean = false,
    val showLongBreakError: Boolean = false,
    val isWebConfirmEnabled: Boolean = true,
    val isMobileConfirmEnabled: Boolean = false,
    val alarmSoundOptions: List<String> = AlarmSound.entries.toTypedArray().map {
        when (it) {
            AlarmSound.CAT -> "Cat"
            AlarmSound.BIRD -> "Bird"
            AlarmSound.BUFFALO -> "Buffalo"
            AlarmSound.DOG -> "Dog"
            AlarmSound.WOLF -> "Wolf"
            AlarmSound.STANDARD -> "Standart"
        }
    },
    val selectedAlarmPos:Int = 0
)