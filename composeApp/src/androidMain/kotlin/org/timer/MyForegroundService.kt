package org.timer

import org.timer.R
import android.app.*
import android.content.*
import android.content.pm.*
import android.os.*
import android.util.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import java.util.*
import java.util.concurrent.*

class MyForegroundService : Service() {

    private var notificationManager: NotificationManagerCompat? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val notificationId = 100

    private var countDownTimer: CountDownTimer? = null
    private var totalDurationMillis: Long = 0
    private var timeLeftInMillis: Long = 0
    private var isTimerFinished = false

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannelId"
        const val ACTION_START = "org.timer.actions.START"
        const val ACTION_STOP = "org.timer.actions.STOP"
        const val ACTION_DISMISS_FINISHED_NOTIFICATION = "org.timer.actions.DISMISS_FINISHED"
        private const val DISMISS_REQUEST_CODE = 101
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Channel for active timer foreground service"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                try {
                    isTimerFinished = false
                    val title = intent.getStringExtra("title") ?: "Timer Active"
                    val newTotalTimeLeftMillis = intent.getLongExtra("totalTimeLeftMillis", 0)
                    val isBreak = intent.getBooleanExtra("isBreak", false)

                    if (newTotalTimeLeftMillis <= 0) {
                        Log.w(
                            "MyForegroundService",
                            "Invalid totalTimeLeftMillis: $newTotalTimeLeftMillis. Stopping."
                        )
                        stopServiceAndNotification()
                        return START_NOT_STICKY
                    }

                    if (countDownTimer != null) {
                        Log.d(
                            "MyForegroundService",
                            "Existing timer found. Stopping it before starting new one."
                        )
                        stopTimer()
                    }

                    totalDurationMillis = newTotalTimeLeftMillis
                    timeLeftInMillis = totalDurationMillis

                    notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.clock)
                        .setContentTitle(title + ": " + formatTimeLeft(timeLeftInMillis))
                        .setProgress(
                            totalDurationMillis.toInt(),
                            (totalDurationMillis - timeLeftInMillis).toInt(),
                            false
                        )
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

                    val notification = notificationBuilder!!.build()

                    ServiceCompat.startForeground(
                        this,
                        notificationId,
                        notification,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        } else {
                            0
                        }
                    )
                    Log.d(
                        "MyForegroundService",
                        "Service started with countdown from $totalDurationMillis ms"
                    )
                    startTimer(title, totalDurationMillis, isBreak)
                } catch (e: Exception) {
                    Log.e("MyForegroundService", "onStartCommand Failed: ${e.message}", e)
                    stopServiceAndNotification()
                }
            }
            ACTION_STOP -> {
                Log.d("MyForegroundService", "Service stopping via STOP action.")
                stopServiceAndNotification()
            }
            ACTION_DISMISS_FINISHED_NOTIFICATION -> {
                Log.d("MyForegroundService", "Dismissing finished notification and stopping service.")
                stopServiceAndNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(title: String, durationMillis: Long, isBreak: Boolean) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isTimerFinished) return
                timeLeftInMillis = millisUntilFinished
                updateNotification(title, false, isBreak)
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                isTimerFinished = true
                updateNotification(title, true, isBreak)
                Log.d("MyForegroundService", "Timer finished. Notification updated to finished state.")
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun updateNotification(title: String, isFinishedState: Boolean, isBreak: Boolean) {
        notificationBuilder?.let { builder ->
            if (isFinishedState) {
                val displayText = if (isBreak) "Meow! Time for another Kittidoro session!" else "Break time! Pick a move and go!"
                builder.setContentTitle(displayText)
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setContentIntent(createDismissPendingIntent())
            } else {
                val currentProgress = (totalDurationMillis - timeLeftInMillis).toInt()
                builder.setContentTitle(title + ": " + formatTimeLeft(timeLeftInMillis))
                    .setProgress(totalDurationMillis.toInt(), currentProgress, false)
                    .setOngoing(true) // Ensure it's ongoing during progress
                    .setAutoCancel(false) // Not auto-cancel during progress
                    .setContentIntent(null) // No content intent during progress
                    .setContentText(null) // Clear any specific content text from finished state
            }
            builder.setShowWhen(false) // Keep this for both states if desired

            try {
                notificationManager?.notify(notificationId, builder.build())
            } catch (ex: Exception) {
                Log.e("MyForegroundService", "Error updating notification: ${ex.message}", ex)
            }
        }
    }

    private fun createDismissPendingIntent(): PendingIntent {
        val dismissIntent = Intent(this, MyForegroundService::class.java).apply {
            action = ACTION_DISMISS_FINISHED_NOTIFICATION
        }
        return PendingIntent.getService(
            this,
            DISMISS_REQUEST_CODE,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTimeLeft(millis: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(millis)

        return if (hours > 0) {
            String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds)
        }
    }

    private fun stopServiceAndNotification() {
        stopTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isTimerFinished = false
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }
}