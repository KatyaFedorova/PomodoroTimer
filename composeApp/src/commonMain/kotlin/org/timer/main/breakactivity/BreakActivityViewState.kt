package org.timer.main.breakactivity

import org.jetbrains.compose.resources.*
import org.timer.main.domain.video.*

data class BreakActivityViewState(
    val screenContent: ScreenContent = ScreenContent.Menu(CurrentMenu(menuItems = emptyList())),
    val showScreenContentOnBigScreen: Boolean = false,
    var showBackButton: Boolean = false,
    var navigateToTimerScreen: Boolean = false,
    var startShortBreak: Boolean = false,
    var startLongBreak: Boolean = false,
    val screenState: ScreenState = ScreenState.PomodoroScreen,
    val showSettingsHint: Boolean = false
)

sealed class ScreenContent {
    data class Menu(val currentMenu: CurrentMenu) : ScreenContent()
    data class VideoContent(val video: Video) : ScreenContent()
    data class AudioContent(val audio: Video) : ScreenContent()
    data class GoForIt(val drawableResource: DrawableResource?) : ScreenContent()
}

sealed class ScreenState {
    data object PomodoroScreen : ScreenState()
    data object ShortBreakScreen: ScreenState()
    data object LongBreakScreen: ScreenState()
}