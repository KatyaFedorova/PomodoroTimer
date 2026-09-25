package org.timer.main.timer

import android.content.Context
import android.content.res.AssetFileDescriptor // Import this
import android.media.MediaPlayer
import android.net.Uri // Still useful for parsing
import android.os.PowerManager
import android.util.Log
import org.timer.main.domain.settings.AlarmSound
import java.io.IOException // Import this

actual class DefaultAlarmPlayer actual constructor(actual val context: Any?) : AlarmPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "AlarmPlayer"

    actual override fun cancel() {
        val playerToRelease = mediaPlayer
        mediaPlayer = null

        playerToRelease?.apply {
            try {
                if (this.isPlaying) {
                    this.stop()
                }
                this.release()
                Log.d(TAG, "MediaPlayer cancelled and released.")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "IllegalStateException during cancel: ${e.message}")
                try { this.release() } catch (ex: Exception) { /* Ignore */ }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during cancel: ${e.message}")
                try { this.release() } catch (ex: Exception) { /* Ignore */ }
            }
        }
    }

    actual override fun play(alarmSound: AlarmSound, onEnded: () -> Unit) {
        if (mediaPlayer != null) {
            Log.d(TAG, "Previous MediaPlayer instance found. Cancelling it.")
            cancel()
        }

        var afd: AssetFileDescriptor? = null
        try {
            val newPlayer = MediaPlayer()
            mediaPlayer = newPlayer
            val androidContext = context as Context

            newPlayer.apply {
                setWakeMode(androidContext, PowerManager.PARTIAL_WAKE_LOCK)

                val resourceUriString = alarmSound.toUri()
                Log.d(TAG, "Received URI string from AlarmSound: $resourceUriString")
                val parsedUri = Uri.parse(resourceUriString)

                if (parsedUri.scheme == "file" && parsedUri.path?.startsWith("/android_asset/") == true) {
                    val assetPath = parsedUri.path?.substring("/android_asset/".length)
                    if (!assetPath.isNullOrEmpty()) {
                        Log.d(TAG, "Attempting to play asset from path: $assetPath")
                        afd = androidContext.assets.openFd(assetPath)
                        setDataSource(afd!!.fileDescriptor, afd!!.startOffset, afd!!.length)
                    } else {
                        throw IOException("Invalid asset path derived from URI: $resourceUriString")
                    }
                } else {
                    Log.d(TAG, "Attempting to play from standard URI: $resourceUriString")
                    setDataSource(androidContext, parsedUri)
                }

                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer playback completed.")
                    onEnded()
                    if (mediaPlayer == this) {
                        this.release()
                        mediaPlayer = null
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer Error for '$resourceUriString': what=$what, extra=$extra")
                    onEnded()
                    if (mediaPlayer == this) {
                        this.release()
                        mediaPlayer = null
                    }
                    true
                }
                prepare()
                start()
                Log.d(TAG, "MediaPlayer started.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "General exception during MediaPlayer setup or playback: ${e.message}", e)
            onEnded()
            mediaPlayer?.release()
            mediaPlayer = null
        } finally {
            try {
                afd?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing AssetFileDescriptor: ${e.message}", e)
            }
        }
    }
}