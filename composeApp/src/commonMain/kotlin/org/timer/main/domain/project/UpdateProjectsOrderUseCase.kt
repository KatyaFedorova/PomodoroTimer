package org.timer.main.domain.project

import kotlinx.coroutines.flow.*

class UpdateProjectsOrderUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(fromIndex: Int, toIndex: Int) {
        val projects = projectsGateway.getProjects().first()
            .toMutableList()
            .apply { add(toIndex, removeAt(fromIndex)) }
        projectsGateway.updateProjects(projects)
    }
}