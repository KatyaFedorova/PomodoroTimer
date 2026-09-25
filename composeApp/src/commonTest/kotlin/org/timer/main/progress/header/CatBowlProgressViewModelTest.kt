package org.timer.main.progress.header

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.game.FakeGameGateway
import org.timer.main.domain.game.*
import org.timer.main.progress.header.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class CatBowlProgressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CatBowlProgressViewModel

    private lateinit var gameGateway: FakeGameGateway
    private lateinit var systamClock: FakeSystemClock

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gameGateway = FakeGameGateway()
        systamClock = FakeSystemClock()
        viewModel = CatBowlProgressViewModel(
            DefaultGetCatBowlFillingProgressUseCase(gameGateway),
            DefaultResetCatBowlFillingProgressUseCase(gameGateway,systamClock)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should show current and total default filling level`() = runTest {
        val initialState = viewModel.viewState.first()
        assertThat(initialState.fillingProgress).isEqualTo(0F)
    }

    @Test
    fun `total filling level from UseCase should show total fulling in view state `() = runTest {
        gameGateway.setProgress(GameGateway.TOTAL_PROGRESS)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.fillingProgress).isEqualTo(1F)
    }

    @Test
    fun `half filling level from UseCase should show half fulling in view state `() = runTest {
        gameGateway.setProgress(2)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.fillingProgress).isEqualTo(0.5F)
    }

    @Test
    fun `not full filling level  should show NOT full bowl view state `() = runTest {
        gameGateway.setProgress(2)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.isBowlFull).isFalse()
    }

    @Test
    fun `full filling level  should show full bowl view state `() = runTest {
        gameGateway.setProgress(GameGateway.TOTAL_PROGRESS)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.isBowlFull).isTrue()
    }
}