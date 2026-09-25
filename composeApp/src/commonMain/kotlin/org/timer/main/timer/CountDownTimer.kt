package org.timer.main.timer

interface CountDownTimer {
    fun startTimer()
    fun pauseTimer()
    fun resetTimer()
    fun isFinished(): Boolean
    fun getCurrentTimeMillis(): Long
}

expect class DefaultCountDownTimer(
    totalMillis: Long,
    onTick: (Long) -> Unit,
    onFinish: () -> Unit,
    isRunning: (Boolean) -> Unit,
) : CountDownTimer {
    var totalMillis: Long
    val onTick: (Long) -> Unit
    val onFinish: () -> Unit
    val isRunning: (Boolean) -> Unit
    var currentMillis: Long

    override fun startTimer()
    override fun pauseTimer()
    override fun resetTimer()
    override fun isFinished(): Boolean
    override fun getCurrentTimeMillis(): Long
}

