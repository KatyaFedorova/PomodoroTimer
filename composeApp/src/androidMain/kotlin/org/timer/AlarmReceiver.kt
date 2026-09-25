package org.timer

import android.content.*
import org.koin.core.component.*
import org.timer.main.domain.settings.*
import org.timer.main.timer.*

const val ALARM_REQUEST_CODE = 123

const val EXTRA_TITLE = "org.timer.main.timer.EXTRA_TITLE"
const val EXTRA_BODY = "org.timer.main.timer.EXTRA_BODY"
const val EXTRA_ALARM_SOUND_NAME = "org.timer.main.timer.EXTRA_ALARM_SOUND_NAME"


class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmSoundName = intent.getStringExtra(EXTRA_ALARM_SOUND_NAME)
            ?: throw IllegalArgumentException("Sound not provided")
        val alarmSound = AlarmSound.valueOf(alarmSoundName)

        val alarmPlayer: AlarmPlayer = get()
        alarmPlayer.play(alarmSound)
    }
}