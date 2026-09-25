package org.timer.main.domain.game

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.game.*
import kotlin.test.*

class GetCatBowlFillingProgressUseCaseTest {

    private lateinit var getCatBowlFillingProgressUseCase: DefaultGetCatBowlFillingProgressUseCase
    private lateinit var gameGateway: FakeGameGateway

    @BeforeTest
    fun setUp() {
        gameGateway = FakeGameGateway()
        getCatBowlFillingProgressUseCase = DefaultGetCatBowlFillingProgressUseCase(gameGateway)
    }

    @Test
    fun `getCatBowlFillingProgress initialy should return default current progress 0`() =
        runTest {
            val actual = getCatBowlFillingProgressUseCase().first()
            assertThat(actual).isEqualTo(0)
        }

    @Test
    fun `getCatBowlFillingProgress  should return updated current and total progress after it was set`() =
        runTest {
            gameGateway.setProgress(4)
            val actual = getCatBowlFillingProgressUseCase().first()
            assertThat(actual).isEqualTo(4)
        }
}