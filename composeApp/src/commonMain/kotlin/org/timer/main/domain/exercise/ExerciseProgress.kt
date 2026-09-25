package org.timer.main.domain.exercise

import org.timer.main.domain.*

data class ExerciseProgress(
    var exercisesCount: Int = 0,
    var lastExercisesUpdateTime: Long = 0L
) {
    fun todayCount(clock: SystemClock): Int {
        if (exercisesCount <= 0 || lastExercisesUpdateTime == 0L) return 0
        return if (clock.isToday(lastExercisesUpdateTime)) exercisesCount else 0
    }
}
