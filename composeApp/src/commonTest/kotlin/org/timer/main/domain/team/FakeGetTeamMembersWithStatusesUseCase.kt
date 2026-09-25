package org.timer.main.domain.team

import kotlinx.coroutines.flow.*

class FakeGetTeamMembersWithStatusesUseCase : GetTeamMembersWithStatusesUseCase {
    val membersFlow = MutableStateFlow<List<TeamMember>>(emptyList())

    override fun invoke(): Flow<List<TeamMember>> = membersFlow
}
