package org.timer.main.timer

class FakeCountDownTimer(
    private var initialMillis: Long,
    private val onTickCallback: ((Long) -> Unit)? = null,
    private val onFinishCallback: (() -> Unit)? = null,
    private val isRunningCallback: ((Boolean) -> Unit)? = null
) : CountDownTimer {
    var startCount = 0
    var pauseCount = 0
    var resetCount = 0

    override fun startTimer() {
        startCount++
        isRunningCallback?.invoke(true)
    }

    override fun pauseTimer() {
        pauseCount++
        isRunningCallback?.invoke(false)
    }

    override fun resetTimer() {
        resetCount++
    }

    override fun isFinished(): Boolean {
        return false
    }

    override fun getCurrentTimeMillis(): Long {
        return initialMillis
    }

    // Test helpers to drive the timer callbacks deterministically.
    fun emitTick(millis: Long) = onTickCallback?.invoke(millis)
    fun finish() = onFinishCallback?.invoke()
}
