package org.timer.main.domain.project

import kotlinx.coroutines.*
import org.timer.main.domain.statistic.*

class DoneTaskUseCase(
    private val projectsGateway: ProjectsGateway,
    private val saveEventUseCase: SaveEventUseCase
) {

    suspend operator fun invoke(projectId: String, taskId: Long) {
        val project = projectsGateway.getProjectById(projectId)
        project.tasks.forEach {
            if (it.id == taskId) it.isDone = true
        }
        projectsGateway.updateProject(project)
        CoroutineScope(Dispatchers.Default).launch {
            saveEventUseCase(EventType.TASK_COMPLETED)
        }
    }
}