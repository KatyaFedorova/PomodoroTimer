package org.timer.main.domain.project

import kotlinx.serialization.*

@Serializable
data class Task(val id: Long, var description: String, var isDone: Boolean)
