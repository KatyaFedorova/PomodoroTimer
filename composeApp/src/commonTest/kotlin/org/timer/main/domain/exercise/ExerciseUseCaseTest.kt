package org.timer.main.domain.exercise

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import kotlin.test.*

class ExerciseUseCaseTest {

    @Test
    fun `incrementing exercises raises the count by one`() = runTest {
        val gateway = FakeExerciseGateway().apply {
            progressFlow.value = ExerciseProgress(exercisesCount = 2, lastExercisesUpdateTime = 1)
        }

        DefaultIncrementExerciseUseCase(gateway, FakeSystemClock()).invoke()

        assertThat(gateway.getProgress().first().exercisesCount).isEqualTo(3)
    }
}
