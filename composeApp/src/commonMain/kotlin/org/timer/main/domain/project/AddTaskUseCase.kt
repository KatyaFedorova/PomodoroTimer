package org.timer.main.domain.project

import org.timer.main.domain.*

class AddTaskUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(projectId: String, taskDescription: String) {
        val project = projectsGateway.getProjectById(projectId)
        val firstDoneTask = project.tasks.firstOrNull { task -> task.isDone }
        val indexOfDoneTask = project.tasks.indexOf(firstDoneTask)
        val newTask = Task(generateID().toLong(), taskDescription, false)
        if (indexOfDoneTask == -1) {
            project.tasks.add(newTask)
        } else {
            project.tasks.add(indexOfDoneTask, newTask)
        }
        projectsGateway.updateProject(project)
    }
}