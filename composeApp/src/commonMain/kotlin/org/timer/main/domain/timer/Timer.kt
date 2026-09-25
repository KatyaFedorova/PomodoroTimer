package org.timer.main.domain.timer

import org.timer.main.domain.*

enum class TimerMode {
    NONE,
    POMODORO,
    SHORT_BREAK,
    LONG_BREAK
}

data class Timer(
    var kittidorosNumber: Int,
    var lastKittidorosUpdateTime: Long,
    var mode: TimerMode = TimerMode.NONE,
    var startAt: Long? = null,
    var durationMillis: Long? = null
) {
    fun isPomodoroRunning(now: Long): Boolean {
        return mode == TimerMode.POMODORO && isStillRunning(now)
    }

    fun isShortBreakRunning(now: Long): Boolean {
        return mode == TimerMode.SHORT_BREAK && isStillRunning(now)
    }

    fun isLongBreakRunning(now: Long): Boolean {
        return mode == TimerMode.LONG_BREAK && isStillRunning(now)
    }

    private fun isStillRunning(now: Long): Boolean {
        val start = startAt ?: return false
        val dur = durationMillis ?: return false
        return (start + dur) > now
    }

    fun getTodayKittidoros(clock: SystemClock): Int {
        if (kittidorosNumber <= 0) return 0
        if (lastKittidorosUpdateTime == 0L) return 0

        return if (clock.isToday(lastKittidorosUpdateTime)) kittidorosNumber else 0
    }
}
