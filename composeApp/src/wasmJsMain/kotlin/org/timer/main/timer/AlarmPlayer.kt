package org.timer.main.timer

import com.diamondedge.logging.*
import org.jetbrains.compose.resources.*
import org.timer.main.domain.settings.*
import org.w3c.dom.*
import pomodorotimer.composeapp.generated.resources.*

@OptIn(ExperimentalResourceApi::class)
actual class DefaultAlarmPlayer actual constructor(actual val context: Any?) : AlarmPlayer {
    private val bird: Audio = Audio(Res.getUri("files/bird.mp3"))
    private val buffalo: Audio = Audio(Res.getUri("files/buffalo.mp3"))
    private val cat: Audio = Audio(Res.getUri("files/cat.mp3"))
    private val dog: Audio = Audio(Res.getUri("files/dog.mp3"))
    private val standart: Audio = Audio(Res.getUri("files/standard.mp3"))
    private val wolf: Audio = Audio(Res.getUri("files/wolf.mp3"))
    private var alarmSound :AlarmSound? = null

    init {
//        bird.load()
//        buffalo.load()
//        cat.load()
//        dog.load()
//        standart.load()
//        wolf.load()
    }
    actual override fun play(alarmSound: AlarmSound, onEnded: () -> Unit) {
        this.alarmSound = alarmSound
        val audio = getAudio(alarmSound)
        playAudio(audio, onEnded)

    }

    private fun playAudio(
        audio: Audio,
        onEnded: () -> Unit
    ) {

        try {
            // Add an error listener
            audio.onerror =
                { event: JsAny?, message: String, lineno: Int, colno: Int, error: JsAny? ->
                    // Log the parameters provided by the binding
                    log.e { "Audio playback error: message=$message, lineno=$lineno, colno=$colno" }
                    if (error != null) {
                        val mediaError = error as? MediaError
                        if (mediaError != null) {
                            log.e { "Error code: ${mediaError.code}" }
                        } else {
                            log.e { "Raw error object: $error" }
                        }
                        log.e { "Raw event object: $event" } // Log the raw object for inspection
                    }

                    // Clean up and invoke onEnded even on error
                    cleanupAudio()
                    log.e { "Audio playback error: onEnded.invoke() " + alarmSound }
                    onEnded.invoke()
                    null
                }

            audio.onended = {
                // Clean up and invoke onEnded when playback finishes
                cleanupAudio()
                log.e { "Audio playback sucsses: onEnded.invoke() " + alarmSound }
                onEnded.invoke()
            }
            log.d { "start playing audio: $alarmSound" }
            audio.play()

        } catch (e: Exception) {
            log.e(e) { "Exception during audio playback setup:  ${e.message}" }
            // Ensure onEnded is called even if setup fails
            onEnded.invoke()
        }
    }

    actual override fun cancel() {
        cleanupAudio()
    }

    private fun cleanupAudio() {
        log.d { "cleanupAudio" }
        alarmSound?.let {
            val audio = getAudio(it)
            audio.apply {
                pause()
                // Remove listeners to prevent memory leaks and unintended behavior
                onended = null
                onerror = null
                // Reset current time (optional, but good practice)
                currentTime = 0.0
            }
        }

    }

    private fun getAudio(alarmSound: AlarmSound): Audio {
        val audio = when (alarmSound) {
            AlarmSound.CAT -> cat
            AlarmSound.BIRD -> bird
            AlarmSound.BUFFALO -> buffalo
            AlarmSound.DOG -> dog
            AlarmSound.WOLF -> wolf
            AlarmSound.STANDARD -> standart
        }
        return audio
    }


    companion object {
        val log = logging("AlarmPlayer")
    }
}