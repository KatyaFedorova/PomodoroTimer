package org.timer.main.domain.project

import kotlinx.coroutines.flow.*

class FakeProjectsGateway : ProjectsGateway {
    val projectsFlow = MutableStateFlow<List<Project>>(emptyList())

    override fun getProjects(): Flow<List<Project>> = projectsFlow

    override suspend fun getProjectById(projectId: String): Project =
        projectsFlow.value.first { it.id == projectId }

    override suspend fun saveProject(project: Project) {
        projectsFlow.value = projectsFlow.value + project
    }

    override suspend fun updateProject(project: Project) {
        projectsFlow.value = projectsFlow.value.map { if (it.id == project.id) project else it }
    }

    override suspend fun updateProjects(projects: List<Project>) {
        projectsFlow.value = projects
    }

    override suspend fun removeProjectById(id: String) {
        projectsFlow.value = projectsFlow.value.filterNot { it.id == id }
    }
}
