package org.timer.main.team.activity

data class PresentableTeamMember(
    val id: String,
    val name: String,
    val onPomodoro: Boolean,
    val onShortBreak: Boolean,
    val onLongBreak: Boolean,
    val aviable: Boolean,
    val kittidorosCount: Int = 0,
    val exercisesCount: Int = 0
)

