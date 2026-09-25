package org.timer.main.domain.project

class MoveTaskToTheEndOfListUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(projectId: String, taskId: Long) {
        val project = projectsGateway.getProjectById(projectId)
        val taskToMove = project.tasks.find { it.id == taskId } ?: return
        project.tasks.remove(taskToMove)
        project.tasks.add(taskToMove)
        projectsGateway.updateProject(project)
    }
}