package org.timer.main.breakactivity

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.*
import org.kodein.emoji.*
import org.kodein.emoji.objects.household.*
import org.kodein.emoji.people_body.person_activity.*
import org.kodein.emoji.people_body.person_resting.*
import org.timer.main.domain.exercise.*
import org.timer.main.domain.game.*
import org.timer.main.domain.settings.*
import org.timer.main.domain.statistic.*
import org.timer.main.domain.video.*
import org.timer.main.domain.video.Video.Category.*
import org.timer.main.domain.video.WorkoutVideosGateway.getClearYourMindVideo
import org.timer.main.domain.video.WorkoutVideosGateway.getDanceAudios
import org.timer.main.domain.video.WorkoutVideosGateway.getWorkoutVideos
import org.timer.main.timer.*
import pomodorotimer.composeapp.generated.resources.*

class BreakActivityViewModel(
    private val incrementCatBowlFillingProgressUseCase: IncrementCatBowlFillingProgressUseCase,
    private val addFishWhenKittyFullUseCase: AddFishWhenKittyFullUseCase,
    private val saveEventUseCase: SaveEventUseCase,
    private val incrementExerciseUseCase: IncrementExerciseUseCase,
    private val settingsGateway: SettingsGateway
) : ViewModel() {
    private val _viewState = MutableStateFlow(BreakActivityViewState())
    val viewState: StateFlow<BreakActivityViewState> = _viewState.asStateFlow()

    private var timerState: TimerState = TimerState.Pomodoro
    private var isVideoEnded = true
    private var isShortBreakTimerRunning = false
    private var canIncrementProgress = true

    private val currentMenu: CurrentMenu
        get() = if (navigationStack.isEmpty()) rootMenu
        else navigationStack.last().children ?: CurrentMenu(menuItems = emptyList())

    private val navigationStack = mutableListOf<MenuItem>()


    private val lowEnergySittingExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == LOW_ENERGY_SITTING }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private val lowEnergyStandingLowKeyExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == LOW_ENERGY_STANDING }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private val lowEnergyStandingPartyEnimalExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == LOW_ENERGY_PARTY_ANIMAL }
            .map { it.toMenuItem() },
        type = Type.GRID
    )


    private val highEnergySittingExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == HIGH_ENERGY_SITTING }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private val highEnergyStandingLowKeyExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == HIGH_ENERGY_STANDING }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private val highEnergyStandingPartyEnimalExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == HIGH_ENERGY_PARTY_ANIMAL }
            .map { it.toMenuItem() },
        type = Type.GRID
    )


    private val highEnergyFloorTimeExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == HIGH_ENERGY_FLOOR_TIME }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private val lowEnergyFloorTimeExercisesMenu = CurrentMenu(
        "Exercises",
        getWorkoutVideos()
            .filter { it.category == LOW_ENERGY_FLOOR_TIME }
            .map { it.toMenuItem() },
        type = Type.GRID
    )

    private fun Video.toMenuItem() = MenuItem(id.toString(), title, imageUrl = thumbnail)

    private val highEnergyChoseYourSpaceMenu = CurrentMenu(
        title = "Chose your space", menuItems = listOf(
            MenuItem(
                id = "1.1.1.1",
                title = "${Emoji.Chair} Sitting",
                subtitle = "For cafes, coworking spaces, or Zoom calls.",
                children = highEnergySittingExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.2",
                title = "${Emoji.Standing} Standing. Low-Key Mode",
                subtitle = "Can move a little but don’t want to draw attention, or a tight spot",
                children = highEnergyStandingLowKeyExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.3",
                title = "${Emoji.ManDancing} Standing. Party animal",
                subtitle = "Got a little room to groove?",
                children = highEnergyStandingPartyEnimalExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.4",
                title = "${Emoji.Yoga} Floor Time",
                subtitle = "Private space? Let’s get on the floor.",
                children = highEnergyFloorTimeExercisesMenu
            ),
        )
    )

    private val lowEnergyChoseYourSpaceMenu = CurrentMenu(
        title = "Chose your space", menuItems = listOf(
            MenuItem(
                id = "1.1.1.1",
                title = "${Emoji.Chair} Sitting",
                subtitle = "For cafes, coworking spaces, or Zoom calls.",
                children = lowEnergySittingExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.2",
                title = "${Emoji.Standing} Standing. Low-Key Mode",
                subtitle = "Can move a little but don’t want to draw attention, or a tight spot",
                children = lowEnergyStandingLowKeyExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.3",
                title = "${Emoji.ManDancing} Standing. Party animal",
                subtitle = "Got a little room to groove?",
                children = lowEnergyStandingPartyEnimalExercisesMenu
            ),
            MenuItem(
                id = "1.1.1.4",
                title = "${Emoji.Yoga} Floor Time",
                subtitle = "Private space? Let’s get on the floor.",
                children = lowEnergyFloorTimeExercisesMenu
            ),
        )
    )

    private val audioMenu = CurrentMenu(
        "Choose your tune",
        getDanceAudios().map { MenuItem(it.id.toString(), it.title) },
        type = Type.AUDIO
    )

    private val energySelectionMenu = CurrentMenu(
        menuItems = listOf(
            MenuItem(
                id = "1.1.1",
                title = "Low energy?",
                subtitle = " 1 exercise for about one minute. Still Counts",
                children = lowEnergyChoseYourSpaceMenu
            ),
            MenuItem(
                id = "1.1.2",
                title = "High Energy?",
                subtitle = "4  minutes of moderate to intense exercises",
                children = highEnergyChoseYourSpaceMenu
            ),
        )
    )

    private val fuelUpMenu = CurrentMenu(
        title = "Fuel up",
        menuItems = listOf(
            MenuItem(
                id = "1.1",
                title = "Get some water",
            ),
            MenuItem(
                id = "1.2",
                title = "Tea/coffee break",
            ),
            MenuItem(
                id = "1.3",
                title = "Snack time",
            ),
        )
    )


    private val moveYourBodyMenu = CurrentMenu(
        menuItems = listOf(
            MenuItem(id = "1.1", title = "Workouts", children = energySelectionMenu),
            MenuItem(id = "1.2", title = "Dance Break", children = audioMenu),
//            MenuItem(id = "1.3", title = "Shake it out"),
//            MenuItem(id = "1.4", title = "Eye Stretch")
        )
    )

    private val tidyUpMenu = CurrentMenu(
        title = "Tidy up",
        menuItems = listOf(
            MenuItem(id = "1.1", title = "Make space tidy"),
            MenuItem(id = "1.2", title = "Take out trash"),
            MenuItem(id = "1.3", title = "Your idea - you know what you are doing"),
        )
    )

    private val rootMenu = CurrentMenu(
        menuItems = listOf(
            MenuItem(
                id = "1", title = "Move Your Body",
                children = moveYourBodyMenu
            ),
            MenuItem(id = "2", title = "Fuel up", children = fuelUpMenu),
            MenuItem(id = "3", title = "Tidy up", children = tidyUpMenu),
            MenuItem(id = "4", title = "Clear your mind"),
            //   MenuItem(id = "5", title = "Mini challenge"),
        )
    )


    init {
        _viewState.update {
            it.copy(
                screenContent = ScreenContent.Menu(currentMenu),
                showBackButton = navigationStack.isNotEmpty()
            )
        }

        viewModelScope.launch {
            settingsGateway.getSettingsHintSeen().collect { seen ->
                _viewState.update { it.copy(showSettingsHint = !seen) }
            }
        }
    }

    fun navigateTo(item: MenuItem) = viewModelScope.launch {
        if (ifMenuHasNextItem(item)) {
            showNextMenuItem(item)
        } else {
            showBackButton()
            showBreakActivityScreen(item)
            startShortOrLongBreak()
        }

    }

    private fun showNextMenuItem(item: MenuItem) {
        navigationStack.add(item)
        _viewState.update {
            it.copy(
                screenContent = ScreenContent.Menu(currentMenu),
                showBackButton = navigationStack.isNotEmpty()
            )
        }
    }

    private fun showBackButton() {
        _viewState.update { it.copy(showBackButton = true) }
    }

    private fun ifMenuHasNextItem(item: MenuItem) = item.children != null

    private fun startShortOrLongBreak() {
        if (timerState is TimerState.Pomodoro ||
            timerState is TimerState.ShortBreak
        ) {
            _viewState.update { it.copy(startShortBreak = true) }
        } else {
            _viewState.update { it.copy(startShortBreak = true) }
        }
    }

    private fun showBreakActivityScreen(item: MenuItem) {
        val screenContent = _viewState.value.screenContent
        if (screenContent is ScreenContent.Menu) {
            when (screenContent.currentMenu.title) {
                "Exercises" -> {
                    onVideoEnded(false)
                    val video = getWorkoutVideos().first { it.id.toString() == item.id }
                    _viewState.update {
                        it.copy(
                            screenContent = ScreenContent.VideoContent(video),
                        )
                    }
                }

                "Choose your tune" -> {
                    onVideoEnded(false)
                    val audio = getDanceAudios().first { it.id.toString() == item.id }
                    _viewState.update {
                        it.copy(screenContent = ScreenContent.AudioContent(audio))
                    }
                }

                else -> {
                    if (item.title == "Clear your mind") {
                        onVideoEnded(false)
                        val video = getClearYourMindVideo()
                        _viewState.update { it.copy(screenContent = ScreenContent.VideoContent(video)) }
                    } else {
                        val drawableRes = resolveDrawableResource(item.title)
                        _viewState.update {
                            it.copy(
                                screenContent = ScreenContent.GoForIt(
                                    drawableRes
                                )
                            )
                        }
                    }

                }
            }
        }
    }

    private fun resolveDrawableResource(title: String): DrawableResource? {
        return when (title) {
            "Get some water" -> Res.drawable.cat_water
            "Tea/coffee break" -> Res.drawable.cat_coffe
            "Snack time" -> Res.drawable.cat_snack
            "Make space tidy" -> Res.drawable.cat_tidy
            "Your idea - you know what you are doing" -> Res.drawable.cat_idea
            "Take out trash" -> Res.drawable.cat_trash
            else -> null
        }
    }

    fun onBackClick() {
        if (_viewState.value.screenContent !is ScreenContent.Menu) {
            _viewState.update {
                it.copy(
                    screenContent = ScreenContent.Menu(currentMenu),
                    showBackButton = navigationStack.isNotEmpty()
                )
            }
            return
        }
        if (navigationStack.isNotEmpty()) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
        _viewState.update {
            it.copy(
                screenContent = ScreenContent.Menu(currentMenu),
                showBackButton = navigationStack.isNotEmpty()
            )
        }
    }

    fun onRandomWorkoutClick() {
        val screenContent = viewState.value.screenContent as ScreenContent.Menu
        val video = when (screenContent.currentMenu) {
            highEnergySittingExercisesMenu -> {
                getWorkoutVideos().filter { it.category == HIGH_ENERGY_SITTING }.random()
            }

            highEnergyStandingLowKeyExercisesMenu -> {
                getWorkoutVideos().filter { it.category == HIGH_ENERGY_STANDING }.random()
            }

            highEnergyStandingPartyEnimalExercisesMenu -> {
                getWorkoutVideos().filter { it.category == HIGH_ENERGY_PARTY_ANIMAL }.random()
            }

            else -> {
                getWorkoutVideos().random()
            }
        }

        onVideoEnded(false)
        _viewState.update {
            it.copy(
                screenContent = ScreenContent.VideoContent(video),
                showBackButton = true,
            )
        }
    }

    private fun navigateToRootMenu() {
        navigationStack.clear()
        _viewState.update {
            it.copy(
                screenContent = ScreenContent.Menu(currentMenu),
                showBackButton = navigationStack.isNotEmpty()
            )
        }
    }

    fun onWatchedVideo(video: Video) = viewModelScope.launch {
        saveEventUseCase(getEventTypeByVideoCategory(video))
        if (canIncrementProgress && video.category != DANCE) {
            canIncrementProgress = false
            incrementExerciseUseCase()
            addFishWhenKittyFullUseCase() // todo make independednt of incrementCatBowlFillingProgressUseCase
            incrementCatBowlFillingProgressUseCase()
        }
    }

    private fun getEventTypeByVideoCategory(video: Video): EventType {
        return when (video.category) {
            HIGH_ENERGY_SITTING -> EventType.EXERCISE_HIGH_ENERGY_COMPLETED
            HIGH_ENERGY_STANDING -> EventType.EXERCISE_HIGH_ENERGY_COMPLETED
            HIGH_ENERGY_PARTY_ANIMAL -> EventType.EXERCISE_HIGH_ENERGY_COMPLETED
            HIGH_ENERGY_FLOOR_TIME -> EventType.EXERCISE_HIGH_ENERGY_COMPLETED
            LOW_ENERGY_SITTING -> EventType.EXERCISE_LOW_ENERGY_COMPLETED
            LOW_ENERGY_STANDING -> EventType.EXERCISE_LOW_ENERGY_COMPLETED
            LOW_ENERGY_PARTY_ANIMAL -> EventType.EXERCISE_LOW_ENERGY_COMPLETED
            LOW_ENERGY_FLOOR_TIME -> EventType.EXERCISE_LOW_ENERGY_COMPLETED
            DANCE -> EventType.DANCE_COMPLETED
            CLEAR_YOUR_MIND -> EventType.CLEAR_YOUR_MIND_COMPLETED
        }
    }


    fun onVideoEnded(isVideoEnded: Boolean) {
        this.isVideoEnded = isVideoEnded
        _viewState.update { it.copy(screenState = getCurrentScreenState(timerState)) }
        if (isPomodoroTimerStateAndVideoEndedOrGoForItScreen()) {
            navigateToRootMenu()
            navigateToTimerScreen()
        }
    }

    fun onTimerStateChanged(timerState: TimerState) {
        this.timerState = timerState
        _viewState.update { it.copy(screenState = getCurrentScreenState(timerState)) }
        if (isPomodoroTimerStateAndVideoEndedOrGoForItScreen()) {
            navigateToRootMenu()
            navigateToTimerScreen()
        }
    }

    private fun getCurrentScreenState(timerState: TimerState): ScreenState {
        if (!isVideoEnded) {
            return ScreenState.ShortBreakScreen
        }
        return when (timerState) {
            is TimerState.Pomodoro -> ScreenState.PomodoroScreen
            is TimerState.ShortBreak -> ScreenState.ShortBreakScreen
            is TimerState.LongBreak -> ScreenState.LongBreakScreen
        }
    }

    private fun navigateToTimerScreen() {
        _viewState.update { it.copy(navigateToTimerScreen = true) }
    }

    private fun isPomodoroTimerStateAndVideoEndedOrGoForItScreen(): Boolean {
        val screenContent = _viewState.value.screenContent
        return this.timerState is TimerState.Pomodoro && isVideoEnded
                || (this.timerState is TimerState.Pomodoro && screenContent is ScreenContent.GoForIt)
    }


    fun onTimerTabClick(index: Int) {
        if (isShortBreakTimerRunning) {
            return
        }
        _viewState.update { it.copy(screenState = getCurrentScreenStateByClickedTab(index)) }
    }

    private fun getCurrentScreenStateByClickedTab(selectedTabIndex: Int): ScreenState {

        return when (selectedTabIndex) {
            0 -> ScreenState.PomodoroScreen
            1 -> ScreenState.ShortBreakScreen
            2 -> ScreenState.LongBreakScreen
            else -> throw IllegalStateException("Wrong selected tab index $selectedTabIndex")
        }
    }

    fun onPomodoroTimerStartRunning() {
        navigateToRootMenu()
    }

    fun onNavigatedToTimerScreen() {
        _viewState.update { it.copy(navigateToTimerScreen = false) }
    }

    fun onShortBreakStarted() {
        _viewState.update { it.copy(startShortBreak = false) }
    }

    fun onLongBreakStarted() {
        _viewState.update { it.copy(startLongBreak = false) }
    }

    fun onShortBreakTimerRunningStateChanged(shortBreakTimerRunning: Boolean) {
        isShortBreakTimerRunning = shortBreakTimerRunning
    }

    fun onKittdoroNuamberChanged(kittyDoroNumber: Int) {
        canIncrementProgress = true
    }

    fun onSettingsHintDismissed() = viewModelScope.launch {
        _viewState.update { it.copy(showSettingsHint = false) }
        settingsGateway.setSettingsHintSeen(true)
    }
}