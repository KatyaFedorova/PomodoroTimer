package org.timer.main.timer

import kotlinx.cinterop.*
import org.timer.main.domain.settings.*
import platform.AVFAudio.*
import platform.AVFoundation.*
import platform.Foundation.*

actual class DefaultAlarmPlayer actual constructor(actual val context: Any?) : AlarmPlayer {

    private var audioPlayer: AVPlayer? = null

    init {
        setUpAudioSession()
    }

    actual override fun play(alarmSound: AlarmSound, onEnded: () -> Unit) {
        val alarmSoundUrl: NSURL? =
            NSURL.URLWithString(alarmSound.toUri())
        alarmSoundUrl?.let { url ->
            audioPlayer = AVPlayer(url)
            NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = audioPlayer?.currentItem,
                queue = NSOperationQueue.mainQueue,
                usingBlock = {
                    onEnded.invoke()
                }
            )
            audioPlayer?.play()
        }
    }

    actual override fun cancel() {
        audioPlayer?.pause()
        audioPlayer = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setUpAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            println("Error setting up audio session: ${e.message}")
        }
    }
}