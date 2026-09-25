package org.timer.main.timer

import kotlinx.cinterop.*
import platform.Foundation.*

actual class DefaultCountDownTimer actual constructor(
    actual var totalMillis: Long,
    actual val onTick: (Long) -> Unit,
    actual val onFinish: () -> Unit,
    actual val isRunning: (Boolean) -> Unit
) : CountDownTimer {
    actual var currentMillis: Long = totalMillis
    private var startMoment: Long = 0L
    private val initalTotalMillis = totalMillis
    private var isTimerRunning = false

    private var iosTimer: NSTimer? = null

    actual override fun startTimer() {
        if (isTimerRunning) return // Prevent starting if already running
        isTimerRunning = true
        isRunning(true)
        // Calculate the target time in milliseconds (future time)
        startMoment = getCurrentTime()

        // Schedule the timer with the given interval
        iosTimer = NSTimer.scheduledTimerWithTimeInterval(
            interval = 1.0,
            repeats = true
        ) { _ ->
            onTimerTick(iosTimer!!)
        }
    }

    @ObjCAction
    fun onTimerTick(timer: NSTimer) {
        // Get the current time in milliseconds
        val currentTimeMillis = getCurrentTime()

        // Calculate the remaining time
        val timePass = ((currentTimeMillis - startMoment).toDouble() / 1000).toLong() * 1000
        currentMillis = totalMillis - timePass

        if (currentMillis <= 0) {
            // If the countdown is complete, call the finish method
            onTick(0)
            onTimerFinish(timer)
            return
        } else {
            onTick(currentMillis)
        }
    }

    private fun getCurrentTime() = (NSDate().timeIntervalSince1970 * 1000).toLong()

    @ObjCAction
    fun onTimerFinish(timer: NSTimer) {
        iosTimer?.invalidate()
        isTimerRunning = false
        isRunning(false)
        currentMillis = 0
        onFinish()
    }

    actual override fun pauseTimer() {
        iosTimer?.invalidate()
        totalMillis = currentMillis
        isTimerRunning = false
        isRunning(false)
    }

    actual override fun resetTimer() {
        iosTimer?.invalidate()
        isTimerRunning = false
        isRunning(false)
        totalMillis = initalTotalMillis
        currentMillis = initalTotalMillis
        onTick.invoke(initalTotalMillis)
    }

    actual override fun isFinished(): Boolean {
        return currentMillis <= 0
    }

    actual override fun getCurrentTimeMillis(): Long = currentMillis
}