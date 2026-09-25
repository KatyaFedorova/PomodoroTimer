package org.timer.main.domain.team

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.js.JsName

@JsName("myAppJsFirebase")
external object MyAppJsFirebase {
    fun listenToMyTeamId(callback: (String?) -> Unit): String
    fun removeMyTeamIdListener(listenerId: String)
    fun listenToTeamMembers(teamId: String, callback: (String) -> Unit): String
    fun removeTeamMembersListener(listenerId: String)
}

// DTO специально для JS-ответа (с doc id)
@Serializable
private data class JsUserDto(
    val id: String,
    val email: String = "",
    val name: String? = null
)

actual class FirebaseTeamGateway : TeamGateway {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getMyTeamId(): Flow<String?> = callbackFlow {
        val listenerId = MyAppJsFirebase.listenToMyTeamId { teamId ->
            trySend(teamId)
        }
        awaitClose { MyAppJsFirebase.removeMyTeamIdListener(listenerId) }
    }

    override fun getMembersByTeamId(teamId: String): Flow<List<User>> = callbackFlow {
        val listenerId = MyAppJsFirebase.listenToTeamMembers(teamId) { jsonStr ->
            val members = runCatching {
                json.decodeFromString(
                    ListSerializer(JsUserDto.serializer()),
                    jsonStr
                ).map { dto ->
                    User(
                        id = dto.id,
                        email = dto.email,
                        name = dto.name
                    )
                }
            }.getOrElse { emptyList() }
            trySend(members)
        }
        awaitClose { MyAppJsFirebase.removeTeamMembersListener(listenerId) }
    }
}
