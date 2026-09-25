package org.timer.main.domain.team

import kotlinx.coroutines.flow.*

class FakeTeamGateway : TeamGateway {
    val myTeamId = MutableStateFlow<String?>(null)
    val membersByTeamId = MutableStateFlow<List<User>>(emptyList())

    override fun getMyTeamId(): Flow<String?> = myTeamId

    override fun getMembersByTeamId(teamId: String): Flow<List<User>> = membersByTeamId
}
