package com.sova.shellclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerForeground : Service() {
    private val CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ShellClock::TimerWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeLeft = intent?.getIntExtra("TIME_LEFT", 0) ?: 0

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        startForeground(NOTIFICATION_ID, buildNotification(formatTime(timeLeft), pendingIntent))

        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            var currentTime = timeLeft
            while (currentTime > 0) {
                delay(1000L)
                currentTime--
                startForeground(NOTIFICATION_ID, buildNotification(formatTime(currentTime), pendingIntent))
            }

            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
                ringtone.play()
                delay(15000L)
                ringtone.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    private fun formatTime(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return when {
            h > 0 -> String.format("Осталось: %dч:%dм:%dс", h, m, s)
            m > 0 -> String.format("Осталось: %dм:%dс", m, s)
            else -> String.format("Осталось: %dс", s)
        }
    }

    private fun buildNotification(text: String, pendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShellClock")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_s)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер ShellClock",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}