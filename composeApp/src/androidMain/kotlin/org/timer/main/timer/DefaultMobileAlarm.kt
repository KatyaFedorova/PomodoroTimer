package org.timer.main.timer

import android.app.*
import android.content.*
import android.util.*
import org.timer.*
import org.timer.main.domain.settings.*

actual class DefaultMobileAlarm actual constructor(actual val context: Any?) : MobileAlarm {

    private val androidContext = context as Context
    private val alarmManager =
        androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    actual override fun schedule(
        scheduledDateTimeMillis: Long,
        alarmSound: AlarmSound,
        title: String,
        body: String
    ) {
        if (alarmManager == null) {
            Log.e("MobileAlarm", "AlarmManager is null, cannot schedule.")
            return
        }

        val intent = Intent(androidContext, AlarmReceiver::class.java).apply {
            // Add extras for the BroadcastReceiver
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_ALARM_SOUND_NAME, alarmSound.name) // Pass enum name
        }

        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            ALARM_REQUEST_CODE, // A unique int for this alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(scheduledDateTimeMillis, null)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.i(
                "MobileAlarm",
                "Alarm scheduled for $scheduledDateTimeMillis with sound ${alarmSound.name}, title: '$title'"
            )
        } catch (se: SecurityException) {
            Log.e(
                "MobileAlarm",
                "SecurityException while scheduling alarm. This is unexpected.",
                se
            )
        } catch (e: Exception) {
            Log.e("MobileAlarm", "Exception while scheduling alarm.", e)
        }
    }

    actual override fun cancel() {
        if (alarmManager == null) {
            Log.w("MobileAlarm", "Cannot cancel alarm: Context or AlarmManager is null.")
            return
        }

        val intent = Intent(androidContext, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i("MobileAlarm", "Alarm cancelled successfully.")
        } else {
            Log.w(
                "MobileAlarm",
                "Alarm could not be cancelled: PendingIntent not found. Was it scheduled with request code $ALARM_REQUEST_CODE?"
            )
        }
    }

    actual override fun startLiveNotification(
        title: String,
        isBreak: Boolean,
        totalTimeLeftMillis: Long,
        alarmSound: AlarmSound
    ) {
        Intent(androidContext, MyForegroundService::class.java).also {
            it.action = MyForegroundService.ACTION_START
            it.putExtra("title", title)
            it.putExtra("totalTimeLeftMillis", totalTimeLeftMillis)
            it.putExtra("isBreak", isBreak)
            androidContext.startService(it)
        }
    }


    actual override fun stopLiveNotification() {
        Intent(androidContext, MyForegroundService::class.java).also {
            it.action = MyForegroundService.ACTION_STOP
            androidContext.startService(it)
        }
    }
}