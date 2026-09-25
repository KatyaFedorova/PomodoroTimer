package org.timer.main.settings

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.timer.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.settings.*
import org.timer.main.settings.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsGateway: FakeSettingsGateway
    private lateinit var playAlarmUseCase: FakePlayAlarmUseCase
    private lateinit var cancelAlarmUseCase: FakeCancelAlarmUseCase
    private lateinit var authGateway: FakeAuthGateway
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsGateway = FakeSettingsGateway()
        playAlarmUseCase = FakePlayAlarmUseCase()
        cancelAlarmUseCase = FakeCancelAlarmUseCase()
        authGateway = FakeAuthGateway()
        viewModel = SettingsViewModel(
            settingsGateway,
            playAlarmUseCase,
            cancelAlarmUseCase,
            DefaultLogoutUseCase(authGateway)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ────────────────────────────── Initial state from settings ──────────────────────────────

    @Test
    fun `selected preset reflects the saved time settings`() = runTest {
        settingsGateway.timeSettingsFlow.value = TimeSettings(1, 0, 0, 0)
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.selectedPresetPosition).isEqualTo(1)
    }

    @Test
    fun `custom time settings are shown as minutes in the text fields`() = runTest {
        settingsGateway.timeSettingsFlow.value =
            TimeSettings(2, 25L * 1000 * 60, 5L * 1000 * 60, 15L * 1000 * 60)
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.pomodoroMinutes).isEqualTo("25")
        assertThat(state.shortBreakMinutes).isEqualTo("5")
        assertThat(state.longBreakMinutes).isEqualTo("15")
    }

    @Test
    fun `selected alarm reflects the saved alarm sound`() = runTest {
        settingsGateway.alarmSoundFlow.value = AlarmSound.WOLF
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.selectedAlarmPos).isEqualTo(AlarmSound.WOLF.ordinal)
    }

    // ────────────────────────────── Presets ──────────────────────────────

    @Test
    fun `choosing the first preset saves the classic pomodoro times`() = runTest {
        advanceUntilIdle()

        viewModel.onPresetClick(0)
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.selectedPresetPosition).isEqualTo(0)
        assertThat(settingsGateway.lastSetTimeSettings)
            .isEqualTo(TimeSettings(0, 25_000L * 60, 5_000L * 60, 15_000L * 60))
    }

    @Test
    fun `choosing the second preset saves the longer times`() = runTest {
        advanceUntilIdle()

        viewModel.onPresetClick(1)
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.selectedPresetPosition).isEqualTo(1)
        assertThat(settingsGateway.lastSetTimeSettings)
            .isEqualTo(TimeSettings(1, 50_000L * 60, 10_000L * 60, 30_000L * 60))
    }

    // ────────────────────────────── Custom minutes ──────────────────────────────

    @Test
    fun `editing the pomodoro minutes saves them as a custom setting`() = runTest {
        advanceUntilIdle()

        viewModel.updatePomodoroMinutes("30")
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.pomodoroMinutes).isEqualTo("30")
        assertThat(state.showPomodoroError).isFalse()
        assertThat(settingsGateway.lastSetTimeSettings)
            .isEqualTo(TimeSettings(2, 30L * 1000 * 60, 0, 0))
    }

    @Test
    fun `clearing the pomodoro minutes shows an error`() = runTest {
        advanceUntilIdle()

        viewModel.updatePomodoroMinutes("")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.showPomodoroError).isTrue()
    }

    // ────────────────────────────── Alarm sound ──────────────────────────────

    @Test
    fun `picking an alarm sound saves it and reflects the selection`() = runTest {
        advanceUntilIdle()

        viewModel.onAlarmSoundClick(AlarmSound.DOG.ordinal)
        advanceUntilIdle()

        assertThat(settingsGateway.lastSetAlarmSound).isEqualTo(AlarmSound.DOG)
        assertThat(viewModel.viewState.value.selectedAlarmPos).isEqualTo(AlarmSound.DOG.ordinal)
    }

    @Test
    fun `previewing the alarm cancels any running alarm before playing`() = runTest {
        advanceUntilIdle()

        viewModel.onPlayAlarmSoundPreviewClick()
        advanceUntilIdle()

        assertThat(cancelAlarmUseCase.cancelCount).isEqualTo(1)
        assertThat(playAlarmUseCase.playCount).isEqualTo(1)
    }

    // ────────────────────────────── Sign out ──────────────────────────────

    @Test
    fun `signing out logs the user out`() = runTest {
        advanceUntilIdle()

        viewModel.onSignOutClick()
        advanceUntilIdle()

        assertThat(authGateway.getAuthState().first()).isEqualTo(AuthState.NotAuthenticated)
    }
}
