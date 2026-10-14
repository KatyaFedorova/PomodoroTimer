package org.timer.main.domain.summary

data class DailyActivity(
    val pomodorosCompleted: Int,
    val tasksCompleted: Int,
    val highEnergyExercises: Int,
    val lowEnergyExercises: Int,
    val dances: Int,
    val clearYourMindSessions: Int,
    val projects: List<ProjectProgress>,
) {
    val breakActivities: Int
        get() = highEnergyExercises + lowEnergyExercises + dances + clearYourMindSessions
}

data class ProjectProgress(
    val name: String,
    val doneTasks: List<String>,
    val openTasks: Int,
)
