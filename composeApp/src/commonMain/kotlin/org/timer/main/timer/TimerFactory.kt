package org.timer.main.timer

interface TimerFactory {
    fun createTimer(
        millisInFuture: Long,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit,
        isRunning: (Boolean) -> Unit
    ): CountDownTimer
}

class DefaultTimerFactory : TimerFactory {
    override fun createTimer(
        millisInFuture: Long,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit,
        isRunning: (Boolean) -> Unit
    ): CountDownTimer {

        return DefaultCountDownTimer(
            millisInFuture,
            onTick = onTick,
            onFinish = onFinish,
            isRunning = isRunning
        )
    }
}