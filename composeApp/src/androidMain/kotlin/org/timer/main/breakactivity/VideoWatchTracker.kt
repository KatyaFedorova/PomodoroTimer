package org.timer.main.breakactivity

import android.os.*
import kotlinx.coroutines.*

/**
 * Трекер реального времени просмотра.
 * Считает секунды ТОЛЬКО когда плеер в состоянии PLAYING.
 * Порог проверяется в живую, пока идёт воспроизведение.
 */
class VideoWatchTracker(
    private val scope: CoroutineScope,
    private val percent: Float = 0.7f,
    private val onWatchedPercent: () -> Unit
) {
    private var duration: Float = 0f

    // накопленные секунды
    private var watchedSeconds: Float = 0f

    // когда начался текущий PLAYING (реальное время)
    private var lastPlayStartRealtimeMs: Long? = null

    private var ticker: Job? = null
    private var triggered = false
    private var playing = false

    fun onDurationKnown(length: Float) {
        duration = length
        checkNow()
    }

    fun onPlay() {
        if (playing) return
        playing = true
        lastPlayStartRealtimeMs = SystemClock.elapsedRealtime()
        startTicker()
    }

    fun onStopLike() {
        if (!playing) return
        // докидываем просмотр за текущую PLAYING-сессию
        lastPlayStartRealtimeMs?.let { start ->
            val now = SystemClock.elapsedRealtime()
            watchedSeconds += (now - start) / 1000f
        }
        lastPlayStartRealtimeMs = null
        playing = false
        stopTicker()
        checkNow()
    }

    fun release() {
        stopTicker()
    }

    private fun startTicker() {
        if (ticker != null) return
        ticker = scope.launch {
            // частый, но лёгкий тик: считаем реальное время, не полагаясь на эвенты
            while (isActive && !triggered) {
                delay(250)
                // временно считаем total = накопленное + текущая сессия
                val total = watchedSeconds + currentSessionSeconds()
                if (duration > 0 && total >= duration * percent) {
                    triggered = true
                    onWatchedPercent()
                    // можно остановить тикер, раз событие уже отправили
                    cancel()
                }
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun currentSessionSeconds(): Float {
        val start = lastPlayStartRealtimeMs ?: return 0f
        val now = SystemClock.elapsedRealtime()
        return (now - start) / 1000f
    }

    private fun checkNow() {
        if (!triggered && duration > 0) {
            val total = watchedSeconds + currentSessionSeconds()
            if (total >= duration * percent) {
                triggered = true
                onWatchedPercent()
            }
        }
    }
}
