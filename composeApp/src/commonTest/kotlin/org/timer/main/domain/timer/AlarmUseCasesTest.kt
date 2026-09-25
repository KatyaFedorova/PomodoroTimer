package org.timer.main.domain.timer

import com.varabyte.truthish.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.settings.*
import org.timer.main.timer.*
import kotlin.test.*

class AlarmUseCasesTest {

    @Test
    fun `playing the alarm plays the configured sound`() = runTest {
        val alarmPlayer = FakeAlarmPlayer()
        val settings = FakeSettingsGateway().apply { alarmSoundFlow.value = AlarmSound.WOLF }

        DefaultPlayAlarmUseCase(alarmPlayer, settings).invoke()

        assertThat(alarmPlayer.playCount).isEqualTo(1)
        assertThat(alarmPlayer.lastPlayedSound).isEqualTo(AlarmSound.WOLF)
    }

    @Test
    fun `playing the alarm forwards the on-ended callback`() = runTest {
        val alarmPlayer = FakeAlarmPlayer()
        val settings = FakeSettingsGateway()
        var ended = false

        DefaultPlayAlarmUseCase(alarmPlayer, settings).invoke(onEnded = { ended = true })

        assertThat(ended).isTrue()
    }

    @Test
    fun `cancelling the alarm cancels the player`() = runTest {
        val alarmPlayer = FakeAlarmPlayer()

        DefaultCancelAlarmUseCase(alarmPlayer).invoke()

        assertThat(alarmPlayer.cancelCount).isEqualTo(1)
    }
}
