package org.timer.main.domain.settings

import org.jetbrains.compose.resources.*
import pomodorotimer.composeapp.generated.resources.*


enum class AlarmSound {
    CAT, BIRD, BUFFALO, DOG, WOLF, STANDARD, ;

    @OptIn(ExperimentalResourceApi::class)
    fun toUri(): String {
        val fileName = when (this) {
            CAT -> "cat.mp3"
            BIRD -> "bird.mp3"
            BUFFALO -> "buffalo.mp3"
            DOG -> "dog.mp3"
            STANDARD -> "standard.mp3"
            WOLF -> "wolf.mp3"
        }
        return Res.getUri("files/$fileName")
    }
}