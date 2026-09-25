package org.timer.main.domain.timer

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.statistic.*
import org.timer.main.domain.timer.*
import kotlin.test.*

class IncrementKittdorsNumberUseCaseTest {

    private lateinit var incrementKittdorsNumberUseCase: DefaultIncrementKittdorsNumberUseCase
    private lateinit var saveEventUseCase: FakeSaveEventUseCase
    private lateinit var systemClock: FakeSystemClock
    private lateinit var timerGateway: FakeTimerGateway

    @BeforeTest
    fun setUp() {
        systemClock = FakeSystemClock()
        timerGateway = FakeTimerGateway()
        saveEventUseCase = FakeSaveEventUseCase()
        incrementKittdorsNumberUseCase =
            DefaultIncrementKittdorsNumberUseCase(timerGateway, systemClock, saveEventUseCase)
    }

    @Test
    fun `invoke should increment kittdors number and last update time`() = runTest {
        systemClock.currentTime = 1000
        incrementKittdorsNumberUseCase.invoke()
        val actual = timerGateway.getTimer().first()
        assertThat(actual).isEqualTo(Timer(1, 1000))
    }

    @Test
    fun `invoke should save pomodoro completed statistic event `() = runTest {
        incrementKittdorsNumberUseCase.invoke()
        val expected: Pair<EventType, Map<String, Any>> = Pair(EventType.POMODORO_COMPLETED, emptyMap())
        assertThat(saveEventUseCase.savedEvent).isEqualTo(expected)
    }
}