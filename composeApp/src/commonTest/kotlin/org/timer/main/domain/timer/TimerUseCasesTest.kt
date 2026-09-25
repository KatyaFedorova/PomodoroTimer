package org.timer.main.domain.timer

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.exercise.*
import kotlin.test.*

class TimerUseCasesTest {

    private lateinit var timerGateway: FakeTimerGateway

    @BeforeTest
    fun setUp() {
        timerGateway = FakeTimerGateway()
    }

    @Test
    fun `starting a pomodoro stores pomodoro mode and duration`() = runTest {
        StartPomodoroUseCase(DefaultStartTimerUseCase(timerGateway)).invoke(60_000L)

        val timer = timerGateway.getTimer().first()
        assertThat(timer.mode).isEqualTo(TimerMode.POMODORO)
        assertThat(timer.durationMillis).isEqualTo(60_000L)
    }

    @Test
    fun `starting a short break stores short break mode`() = runTest {
        StartShortBreakUseCase(DefaultStartTimerUseCase(timerGateway)).invoke(5_000L)

        assertThat(timerGateway.getTimer().first().mode).isEqualTo(TimerMode.SHORT_BREAK)
    }

    @Test
    fun `starting a long break stores long break mode`() = runTest {
        StartLongBreakUseCase(DefaultStartTimerUseCase(timerGateway)).invoke(15_000L)

        assertThat(timerGateway.getTimer().first().mode).isEqualTo(TimerMode.LONG_BREAK)
    }

    @Test
    fun `stopping the timer clears the mode and duration`() = runTest {
        StartPomodoroUseCase(DefaultStartTimerUseCase(timerGateway)).invoke(60_000L)

        DefaultStopTimerUseCase(timerGateway).invoke()

        val timer = timerGateway.getTimer().first()
        assertThat(timer.mode).isEqualTo(TimerMode.NONE)
        assertThat(timer.durationMillis).isEqualTo(0L)
    }

    @Test
    fun `resetting kittidoros zeroes the count when it was not updated today`() = runTest {
        timerGateway.updateTimer(Timer(kittidorosNumber = 5, lastKittidorosUpdateTime = 123))

        DefaultResetKittidoroNumberUseCase(timerGateway, FakeSystemClock()).invoke()

        assertThat(timerGateway.getTimer().first().kittidorosNumber).isEqualTo(0)
    }

    @Test
    fun `getting the kittidoro number reflects the stored timer`() = runTest {
        timerGateway.updateTimer(Timer(kittidorosNumber = 7, lastKittidorosUpdateTime = 1))

        val number = DefaultGetKittidorosNumberUseCase(timerGateway).invoke().first()

        assertThat(number).isEqualTo(7)
    }

    @Test
    fun `resetting exercises zeroes the count when it was not updated today`() = runTest {
        val exerciseGateway = FakeExerciseGateway().apply {
            progressFlow.value = ExerciseProgress(exercisesCount = 4, lastExercisesUpdateTime = 123)
        }

        DefaultResetExercisesNumberUseCase(exerciseGateway, FakeSystemClock()).invoke()

        assertThat(exerciseGateway.getProgress().first().exercisesCount).isEqualTo(0)
    }
}
