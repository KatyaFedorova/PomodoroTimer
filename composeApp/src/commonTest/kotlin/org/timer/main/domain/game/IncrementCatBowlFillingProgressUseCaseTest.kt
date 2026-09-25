package org.timer.main.domain.game

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import kotlin.test.*

class IncrementCatBowlFillingProgressUseCaseTest {

    private lateinit var incrementCatBowlFillingProgressUseCase: DefaultIncrementCatBowlFillingProgressUseCase
    private lateinit var fakeGameGateway: FakeGameGateway
    private lateinit var fakeSystemClock: FakeSystemClock

    @BeforeTest
    fun setUp() {
        fakeGameGateway = FakeGameGateway()
        fakeSystemClock = FakeSystemClock()
        incrementCatBowlFillingProgressUseCase =
            DefaultIncrementCatBowlFillingProgressUseCase(fakeGameGateway, fakeSystemClock)
    }

    @Test
    fun `invoke should  incrementCatBowlFillingProgress`() = runTest {
        fakeGameGateway.setProgress(0)
        incrementCatBowlFillingProgressUseCase.invoke()
        val actual = fakeGameGateway.getBowlFillingProgress().first()
        assertThat(actual).isEqualTo(1)
    }

    @Test
    fun `invoke should  incrementCatBowlFillingProgress from 3 to TOTAL_PROGRESS`() = runTest {
        fakeGameGateway.setProgress(3)
        incrementCatBowlFillingProgressUseCase.invoke()
        val actual = fakeGameGateway.getBowlFillingProgress().first()
        assertThat(actual).isEqualTo(4)
    }

    @Test
    fun `progress should not exceed total`() = runTest {
        fakeGameGateway.setProgress(GameGateway.TOTAL_PROGRESS)
        incrementCatBowlFillingProgressUseCase.invoke()
        val actual = fakeGameGateway.getBowlFillingProgress().first()
        assertThat(actual).isEqualTo(GameGateway.TOTAL_PROGRESS)
    }

    @Test
    fun `IncrementCatBowlProgressUseCase should save current date when kitty is full`() = runTest {
        fakeGameGateway.setProgress(3)
        fakeSystemClock.currentTime = 1000
        incrementCatBowlFillingProgressUseCase.invoke()
        val actualList = fakeGameGateway.getKittyFeedDates().first()
        val exactedList = listOf(1000L)
        assertThat(actualList.first()).isEqualTo(exactedList.first())
    }
}