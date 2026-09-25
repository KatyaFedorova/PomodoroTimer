package org.timer.main.domain.project

import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import kotlin.js.Promise

// JS interop интерфейсы
external interface JsResult : kotlin.js.JsAny {
    val success: Boolean
    val error: String?
}

external interface JsProjectResult : JsResult {
    val projectJson: String?
}

@JsName("myAppJsFirebase")
external object MyAppJsFirebase {
    fun listenToProjects(callback: (String) -> Unit): String
    fun removeProjectListener(listenerId: String)

    fun getProjectById(projectId: String): Promise<JsProjectResult>
    fun saveProject(projectJson: String): Promise<JsResult>
    fun updateProjects(projectsJson: String): Promise<JsResult>
    fun removeProjectById(id: String): Promise<JsResult>
}


actual class FirebaseProjectsGateway : ProjectsGateway {

    private val json = Json { ignoreUnknownKeys = true }

    // local cache
    private val _projects = MutableStateFlow<List<Project>>(emptyList())

    init {
        CoroutineScope(Dispatchers.Default).launch {
            callbackFlow {
                val listenerId = MyAppJsFirebase.listenToProjects { jsonString ->
                    val projects = json.decodeFromString<List<Project>>(jsonString)
                    trySend(projects.sortedBy { it.order })
                }
                awaitClose { MyAppJsFirebase.removeProjectListener(listenerId) }
            }.collect {
                _projects.value = it
            }
        }
    }

    actual override fun getProjects(): Flow<List<Project>> = callbackFlow {
        val listenerId = MyAppJsFirebase.listenToProjects { jsonString ->
            val projects = json.decodeFromString<List<Project>>(jsonString)
            trySend(projects.sortedBy { it.order })
        }
        awaitClose {
            MyAppJsFirebase.removeProjectListener(listenerId)
        }
    }

    actual override suspend fun getProjectById(projectId: String): Project {
        val result: JsProjectResult = MyAppJsFirebase.getProjectById(projectId).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to get project by ID")
        }
        val projectJson = result.projectJson
            ?: throw Exception("Project JSON is null")
        return json.decodeFromString(projectJson)
    }

    actual override suspend fun saveProject(project: Project) {
        logger.d { "saveProject" + project }
        val size = _projects.value.size
        val projectJson = json.encodeToString(Project.serializer(), project.copy(order = size))
        val result: JsResult = MyAppJsFirebase.saveProject(projectJson).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to save project")
        }
    }


    actual override suspend fun updateProject(project: Project) {
        logger.d { "updateProject" + project }
        val projectJson = json.encodeToString(Project.serializer(), project)
        val result: JsResult = MyAppJsFirebase.saveProject(projectJson).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to update project")
        }
    }

    actual override suspend fun updateProjects(projects: List<Project>) {
        val indexedProjects = projects.mapIndexed { index, project -> project.copy(order = index) }
        val projectsJson = json.encodeToString(ListSerializer(Project.serializer()), indexedProjects)
        logger.d { "updateProjects" + projectsJson }
        val result: JsResult = MyAppJsFirebase.updateProjects(projectsJson).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to update projects")
        }
    }

    actual override suspend fun removeProjectById(id: String) {
        val result: JsResult = MyAppJsFirebase.removeProjectById(id).await()
        if (!result.success) {
            throw Exception(result.error ?: "Failed to remove project")
        }
    }

    companion object {
        private val logger = logging()
    }
}
