package org.timer.main.domain.project

import org.timer.main.domain.*

class AddProjectUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(name: String) {
        val project = Project(generateID(), name, mutableListOf())
        projectsGateway.saveProject(project)
    }
}