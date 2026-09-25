package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*

interface TimerGateway {
    fun getTimer(): Flow<Timer>
    fun getTimerByUserId(userId: String): Flow<Timer>

    suspend fun updateTimer(timer: Timer)
}

