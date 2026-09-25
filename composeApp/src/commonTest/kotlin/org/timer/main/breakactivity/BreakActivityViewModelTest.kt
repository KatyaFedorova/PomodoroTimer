package org.timer.main.breakactivity

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.game.FakeGameGateway
import org.timer.main.breakactivity.*
import org.timer.main.domain.exercise.*
import org.timer.main.domain.game.*
import org.timer.main.domain.settings.*
import org.timer.main.domain.statistic.*
import org.timer.main.domain.video.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class BreakActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var gameGateway: FakeGameGateway
    private lateinit var saveEventUseCase: FakeSaveEventUseCase
    private lateinit var incrementExerciseUseCase: FakeIncrementExerciseUseCase
    private lateinit var settingsGateway: FakeSettingsGateway
    private lateinit var viewModel: BreakActivityViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gameGateway = FakeGameGateway()
        saveEventUseCase = FakeSaveEventUseCase()
        incrementExerciseUseCase = FakeIncrementExerciseUseCase()
        settingsGateway = FakeSettingsGateway()
        viewModel = BreakActivityViewModel(
            DefaultIncrementCatBowlFillingProgressUseCase(gameGateway, FakeSystemClock()),
            DefaultAddFishWhenKittyFullUseCase(gameGateway),
            saveEventUseCase,
            incrementExerciseUseCase,
            settingsGateway,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun currentMenu(): CurrentMenu =
        (viewModel.viewState.value.screenContent as ScreenContent.Menu).currentMenu

    // ────────────────────────────── Menu navigation ──────────────────────────────

    @Test
    fun `initial state shows the root menu with no back button`() = runTest {
        advanceUntilIdle()
        val menu = currentMenu()

        assertThat(menu.title).isNull()
        assertThat(menu.menuItems.size).isEqualTo(4)
        assertThat(menu.menuItems.any { it.title == "Move Your Body" }).isTrue()
        assertThat(viewModel.viewState.value.showBackButton).isFalse()
    }

    @Test
    fun `opening a parent menu shows its children and the back button`() = runTest {
        advanceUntilIdle()

        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Move Your Body" })
        advanceUntilIdle()

        assertThat(currentMenu().menuItems.any { it.title == "Workouts" }).isTrue()
        assertThat(viewModel.viewState.value.showBackButton).isTrue()
    }

    @Test
    fun `going back from a sub menu returns to the root menu`() = runTest {
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Move Your Body" })
        advanceUntilIdle()

        viewModel.onBackClick()
        advanceUntilIdle()

        assertThat(currentMenu().menuItems.any { it.title == "Move Your Body" }).isTrue()
        assertThat(viewModel.viewState.value.showBackButton).isFalse()
    }

    @Test
    fun `going back walks up the navigation stack one level at a time`() = runTest {
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Move Your Body" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Workouts" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Low energy?" })
        advanceUntilIdle()
        assertThat(currentMenu().title).isEqualTo("Chose your space")

        viewModel.onBackClick()
        advanceUntilIdle()
        assertThat(currentMenu().menuItems.any { it.title == "Low energy?" }).isTrue()

        viewModel.onBackClick()
        advanceUntilIdle()
        assertThat(currentMenu().menuItems.any { it.title == "Workouts" }).isTrue()

        viewModel.onBackClick()
        advanceUntilIdle()
        assertThat(currentMenu().menuItems.any { it.title == "Move Your Body" }).isTrue()
        assertThat(viewModel.viewState.value.showBackButton).isFalse()
    }

    // ────────────────────────────── Leaf selections ──────────────────────────────

    @Test
    fun `selecting an exercise opens the video screen`() = runTest {
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Move Your Body" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Workouts" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Low energy?" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title.contains("Sitting") })
        advanceUntilIdle()

        val exercise = currentMenu().menuItems.first()
        viewModel.navigateTo(exercise)
        advanceUntilIdle()

        val content = viewModel.viewState.value.screenContent
        assertThat(content is ScreenContent.VideoContent).isTrue()
        assertThat((content as ScreenContent.VideoContent).video.id.toString()).isEqualTo(exercise.id)
        assertThat(viewModel.viewState.value.showBackButton).isTrue()
    }

    @Test
    fun `selecting a dance tune opens the audio screen`() = runTest {
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Move Your Body" })
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Dance Break" })
        advanceUntilIdle()

        val tune = currentMenu().menuItems.first()
        viewModel.navigateTo(tune)
        advanceUntilIdle()

        val content = viewModel.viewState.value.screenContent
        assertThat(content is ScreenContent.AudioContent).isTrue()
        assertThat((content as ScreenContent.AudioContent).audio.id.toString()).isEqualTo(tune.id)
    }

    @Test
    fun `selecting a fuel-up activity opens the go-for-it screen and starts a break`() = runTest {
        advanceUntilIdle()
        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Fuel up" })
        advanceUntilIdle()

        viewModel.navigateTo(currentMenu().menuItems.first { it.title == "Get some water" })
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.screenContent is ScreenContent.GoForIt).isTrue()
        assertThat(state.startShortBreak).isTrue()
        assertThat(state.showBackButton).isTrue()
    }

    @Test
    fun `random workout opens a video screen`() = runTest {
        advanceUntilIdle()

        viewModel.onRandomWorkoutClick()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.screenContent is ScreenContent.VideoContent).isTrue()
        assertThat(state.showBackButton).isTrue()
    }

    // ────────────────────────────── Watching a video ──────────────────────────────

    @Test
    fun `watching an exercise video saves the event and fills the cat bowl`() = runTest {
        advanceUntilIdle()
        gameGateway.setProgress(0)

        val video = WorkoutVideosGateway.getWorkoutVideos().first() // an exercise (non-dance)
        viewModel.onWatchedVideo(video)
        advanceUntilIdle()

        assertThat(saveEventUseCase.savedEvent).isNotNull()
        assertThat(incrementExerciseUseCase.invokeCount).isEqualTo(1)
        assertThat(gameGateway.getBowlFillingProgress().first()).isEqualTo(1)
    }

    @Test
    fun `watching a dance video does not fill the cat bowl`() = runTest {
        advanceUntilIdle()
        gameGateway.setProgress(0)

        val dance = WorkoutVideosGateway.getDanceAudios().first() // DANCE category
        viewModel.onWatchedVideo(dance)
        advanceUntilIdle()

        assertThat(saveEventUseCase.savedEvent).isNotNull()
        assertThat(incrementExerciseUseCase.invokeCount).isEqualTo(0)
        assertThat(gameGateway.getBowlFillingProgress().first()).isEqualTo(0)
    }

    // ────────────────────────────── Settings hint ──────────────────────────────

    @Test
    fun `settings hint is shown until it has been seen`() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.showSettingsHint).isTrue()
    }

    @Test
    fun `dismissing the settings hint hides it and remembers the choice`() = runTest {
        advanceUntilIdle()

        viewModel.onSettingsHintDismissed()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.showSettingsHint).isFalse()
        assertThat(settingsGateway.getSettingsHintSeen().first()).isTrue()
    }
}
