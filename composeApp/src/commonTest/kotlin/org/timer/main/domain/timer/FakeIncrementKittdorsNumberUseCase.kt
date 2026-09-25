package org.timer.main.domain.timer

class FakeIncrementKittdorsNumberUseCase(
    private val getKittidorosNumberUseCase: FakeGetKittidorosNumberUseCase
) : IncrementKittdorsNumberUseCase {
    var invokeCount = 0

    override suspend fun invoke() {
        invokeCount++
        getKittidorosNumberUseCase.kittidoros.value += 1
    }
}
