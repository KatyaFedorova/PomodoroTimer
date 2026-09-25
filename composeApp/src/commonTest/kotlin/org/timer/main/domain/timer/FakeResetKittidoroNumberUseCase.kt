package org.timer.main.domain.timer

class FakeResetKittidoroNumberUseCase : ResetKittidoroNumberUseCase {
    var invokeCount = 0

    override suspend fun invoke() {
        invokeCount++
    }
}
