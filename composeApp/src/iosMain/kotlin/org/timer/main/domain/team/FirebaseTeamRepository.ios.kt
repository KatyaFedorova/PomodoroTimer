package org.timer.main.domain.team

import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.*

actual class FirebaseTeamGateway : TeamGateway {

    private val userId: String
        get() = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User is not authenticated")

    override fun getMyTeamId(): Flow<String?> =
        Firebase.auth.authStateChanged
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(null)
                } else {
                    Firebase.firestore
                        .collection("users")
                        .document(userId)
                        .snapshots
                        .map { snap ->
                            if (snap.exists) snap.data<UserDto>().teamId else null
                        }
                }
            }


    override fun getMembersByTeamId(teamId: String): Flow<List<User>> =
        Firebase.firestore
            .collection("users")
            .where { "teamId" equalTo teamId }
            .snapshots
            .map { qs ->
                qs.documents.map { doc ->
                    val dto = doc.data<UserDto>()
                    User(
                        id = doc.id,
                        email = dto.email,
                        name = dto.name
                    )
                }
            }
}
