package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*

class FakeTimerGateway : TimerGateway {

    private val flow = MutableStateFlow(Timer(0, 0))
    private val byUser = mutableMapOf<String, MutableStateFlow<Timer>>()

    override fun getTimer(): Flow<Timer> {
        return flow
    }

    override fun getTimerByUserId(userId: String): Flow<Timer> {
        return byUser.getOrPut(userId) { MutableStateFlow(Timer(0, 0)) }
    }

    override suspend fun updateTimer(timer: Timer) {
        flow.emit(timer)
    }

    fun setTimerForUser(userId: String, timer: Timer) {
        byUser.getOrPut(userId) { MutableStateFlow(Timer(0, 0)) }.value = timer
    }
}
