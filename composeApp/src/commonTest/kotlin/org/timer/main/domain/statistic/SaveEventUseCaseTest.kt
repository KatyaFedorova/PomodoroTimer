package org.timer.main.domain.statistic

import com.varabyte.truthish.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.statistic.*
import kotlin.test.*

class SaveEventUseCaseTest {

    private lateinit var saveEventUseCase: DefaultSaveEventUseCase
    private lateinit var statisticsEventGateway: FakeStatisticsEventGateway
    private lateinit var fakeSystemClock: FakeSystemClock

    @BeforeTest
    fun setUp() {
        statisticsEventGateway = FakeStatisticsEventGateway()
        fakeSystemClock = FakeSystemClock()
        saveEventUseCase = DefaultSaveEventUseCase(statisticsEventGateway, fakeSystemClock)
    }

    @Test
    fun `invoke should save event`() = runTest {
        fakeSystemClock.currentTimeZone = "Europe/Warsaw"
        saveEventUseCase.invoke(EventType.POMODORO_COMPLETED, emptyMap())

        val expectedEvent = Event(EventType.POMODORO_COMPLETED, emptyMap(), null, "Europe/Warsaw")
        assertThat(statisticsEventGateway.savedEvent).isEqualTo(expectedEvent)
    }
}