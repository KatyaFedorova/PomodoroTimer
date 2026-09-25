package org.timer.main.domain.game

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.game.FakeGameGateway
import org.timer.main.domain.game.*
import kotlin.test.*

class GameUseCasesTest {

    private lateinit var gameGateway: FakeGameGateway
    private lateinit var clock: FakeSystemClock

    @BeforeTest
    fun setUp() {
        gameGateway = FakeGameGateway()
        clock = FakeSystemClock()
    }

    @Test
    fun `getting the fish number returns what the gateway holds`() = runTest {
        gameGateway.setFishNumber(3)

        assertThat(DefaultGetFishesNumberUseCase(gameGateway).invoke().first()).isEqualTo(3)
    }

    @Test
    fun `adding a fish when the bowl is full increases the fish count`() = runTest {
        gameGateway.setProgress(GameGateway.TOTAL_PROGRESS)

        DefaultAddFishWhenKittyFullUseCase(gameGateway).invoke()

        assertThat(gameGateway.getFishNumber().first()).isEqualTo(1)
    }

    @Test
    fun `adding a fish when the bowl is not full does nothing`() = runTest {
        gameGateway.setProgress(0)

        DefaultAddFishWhenKittyFullUseCase(gameGateway).invoke()

        assertThat(gameGateway.getFishNumber().first()).isEqualTo(0)
    }

    @Test
    fun `resetting the bowl clears progress when it was not reset today`() = runTest {
        gameGateway.setProgress(3)

        DefaultResetCatBowlFillingProgressUseCase(gameGateway, clock).invoke()

        assertThat(gameGateway.getBowlFillingProgress().first()).isEqualTo(0)
    }
}
