package org.timer.main.projects

import org.timer.main.domain.project.*

data class PresentableProject(
    val id: String,
    val name: String,
    val tasks: MutableList<Task>,
    val showAddTaskFooter: Boolean = false
)