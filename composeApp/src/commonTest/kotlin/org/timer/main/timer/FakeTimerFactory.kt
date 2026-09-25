package org.timer.main.timer

class FakeTimerFactory : TimerFactory {
    val createdTimers = mutableListOf<FakeCountDownTimer>()

    override fun createTimer(
        millisInFuture: Long,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit,
        isRunning: (Boolean) -> Unit
    ): CountDownTimer {
        val fakeTimer = FakeCountDownTimer(
            initialMillis = millisInFuture,
            onTickCallback = onTick,
            onFinishCallback = onFinish,
            isRunningCallback = isRunning
        )
        createdTimers.add(fakeTimer)
        return fakeTimer
    }
}
