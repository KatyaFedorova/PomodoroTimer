package org.timer.main.domain.timer

import org.timer.main.domain.timer.*

class FakeCancelAlarmUseCase : CancelAlarmUseCase {
    var cancelCount = 0

    override fun invoke() {
        cancelCount++
    }
}