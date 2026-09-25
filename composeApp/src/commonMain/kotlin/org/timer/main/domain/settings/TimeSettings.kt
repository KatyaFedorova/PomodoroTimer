package org.timer.main.domain.settings

import kotlinx.serialization.*

@Serializable
data class TimeSettings(
    val selectedPosition: Int,
    val pomodoroTime: Long,
    val shortBreakTime: Long,
    val longBreakTime: Long
)
