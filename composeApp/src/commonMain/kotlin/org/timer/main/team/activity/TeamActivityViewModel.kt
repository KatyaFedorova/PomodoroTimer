package org.timer.main.team.activity

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.team.*

class TeamActivityViewModel(
    private val getTeamMembersWithStatusesUseCase: GetTeamMembersWithStatusesUseCase
) : ViewModel() {
    private val _viewState = MutableStateFlow(TeamActivityViewState())
    val viewState: StateFlow<TeamActivityViewState> = _viewState.asStateFlow()

    init {
        getTeamMembersWithStatusesUseCase().onEach { members ->
            val presentableMembers = members
                .map { it.toPresentable() }
                .sortedBy { it.name }
            _viewState.update { it.copy(members = presentableMembers) }
        }.launchIn(viewModelScope)
    }


}

private fun TeamMember.toPresentable(): PresentableTeamMember {
    return PresentableTeamMember(
        this.userId,
        this.name ?: this.email,
        this.isPomodoroRunning,
        this.isShortBreakRunning,
        this.isLongBreakRunning,
        !isPomodoroRunning && !isShortBreakRunning && !isLongBreakRunning,
        this.kittidoros,
        this.exerciseCount
    )
}

