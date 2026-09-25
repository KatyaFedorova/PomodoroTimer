package org.timer.main.domain.game

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.game.*
import kotlin.test.*

class GetFeedingDatesUseCaseTest {

    private lateinit var getFeedingDatesUseCase: DefaultGetFeedingDatesUseCase
    private lateinit var gameGateway: GameGateway

    @BeforeTest
    fun setUp() {
        gameGateway = FakeGameGateway()
        getFeedingDatesUseCase = DefaultGetFeedingDatesUseCase(gameGateway)
    }

    @Test
    fun `invoke should return list of saved kitty feed dates`() = runTest {
        gameGateway.saveKittyFeedDate(1000L)
        gameGateway.saveKittyFeedDate(2000L)
        gameGateway.saveKittyFeedDate(3000L)

        val result = getFeedingDatesUseCase.invoke().first()
        assertThat(result).isEqualTo(listOf(1000L, 2000L, 3000L))

    }
}