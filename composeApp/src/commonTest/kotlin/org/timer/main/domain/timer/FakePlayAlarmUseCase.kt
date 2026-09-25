package org.timer.main.domain.timer

import org.timer.main.domain.timer.*

class FakePlayAlarmUseCase : PlayAlarmUseCase {
    var playCount = 0

    override suspend fun invoke(onEnded: () -> Unit) {
        playCount++
        onEnded.invoke()
    }
}