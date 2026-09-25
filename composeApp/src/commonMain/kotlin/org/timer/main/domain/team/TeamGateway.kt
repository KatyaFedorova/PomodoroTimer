package org.timer.main.domain.team

import kotlinx.coroutines.flow.*

interface TeamGateway {
     fun getMyTeamId(): Flow<String?>

     fun getMembersByTeamId(teamId: String): Flow<List<User>>
}