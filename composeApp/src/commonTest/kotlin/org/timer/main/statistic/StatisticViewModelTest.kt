package org.timer.main.statistic

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.game.FakeGameGateway
import org.timer.main.domain.game.*
import org.timer.main.statistic.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class StatisticViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: StatisticViewModel

    private lateinit var gameGateway: FakeGameGateway

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gameGateway = FakeGameGateway()
        viewModel = StatisticViewModel(
            DefaultGetFeedingDatesUseCase(gameGateway),
            DefaultGetFishesNumberUseCase(gameGateway)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should show  empty list of dates`() = runTest {
        val initialState = viewModel.viewState.first()
        assertThat(initialState.feedingDates).isEqualTo(emptyList<Long>())
    }

    @Test
    fun `screen should show list of feeding dates when they are present`() = runTest {
        gameGateway.saveKittyFeedDate(1000L)
        gameGateway.saveKittyFeedDate(2000L)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.feedingDates).isEqualTo(listOf(1000L, 2000L))
    }


}