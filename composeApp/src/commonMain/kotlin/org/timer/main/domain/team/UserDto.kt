package org.timer.main.domain.team

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val email: String = "",
    val name: String? = null,
    val teamId: String? = null
)