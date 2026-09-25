package org.timer.main.domain.timer

import kotlinx.coroutines.flow.*

class FakeGetKittidorosNumberUseCase : GetKittidorosNumberUseCase {
    val kittidoros = MutableStateFlow(0)

    override fun invoke(): Flow<Int> = kittidoros
}
