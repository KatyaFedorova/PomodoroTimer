package org.timer.main.domain.project

import com.diamondedge.logging.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.*

actual class FirebaseProjectsGateway : ProjectsGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")


    private val projectsCollection = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("projects")


    actual override fun getProjects(): Flow<List<Project>> {
        return Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(emptyList())
                } else {
                    projectsCollection
                        .snapshots
                        .catch { logger.e(it) { "Error while getting projects" } }
                        .map { snapshot ->
                            snapshot.documents.map { doc ->
                                doc.data<Project>()
                            }.sortedBy { it.order }
                        }
                }
            }
    }

    actual override suspend fun getProjectById(projectId: String): Project {
        return projectsCollection.document(projectId).get().data<Project>()
    }

    actual override suspend fun saveProject(project: Project) {
        val snapshot = projectsCollection
            .orderBy("order", Direction.DESCENDING)
            .limit(1)
            .get()

        val maxOrder = snapshot.documents.firstOrNull()
            ?.data<Project>()
            ?.order ?: -1
        val newOrder = maxOrder + 1

        projectsCollection.document(project.id).set(project.copy(order = newOrder))
    }

    actual override suspend fun updateProject(project: Project) {
        projectsCollection.document(project.id).set(project)
    }

    actual override suspend fun updateProjects(projects: List<Project>) {
        logger.d { "updateProjects $projects" }
        val batch = Firebase.firestore.batch()
        projects.forEachIndexed { index, project ->
            val docRef = Firebase.firestore
                .collection("users")
                .document(userId)
                .collection("projects")
                .document(project.id)
            val projectWithOrder = project.copy(order = index)
            batch.set(docRef, projectWithOrder)
        }
        batch.commit()
    }

    actual override suspend fun removeProjectById(id: String) {
        projectsCollection.document(id).delete()
    }

    private companion object {
        val logger = logging("FirebaseProjectsGateway")
    }
}