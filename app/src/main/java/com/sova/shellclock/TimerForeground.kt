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
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerForeground : Service() {
    private val CHANNEL_ID = "timer_channel"
    private val DONE_CHANNEL_ID = "timer_done_channel"
    private val NOTIFICATION_ID = 2
    private val DONE_NOTIFICATION_ID = 3
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ShellClock::TimerWakeLock"
        )
        wakeLock?.apply {
            // setReferenceCounted(false): onDestroy освобождает ровно один раз, иначе строка «release without a matched acquire»
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeLeft = intent?.getIntExtra("TIME_LEFT", 0) ?: 0
        // Считаем по монотонным часам, а не по числу тиков: delay(1000) во сне и при Doze копится отставанием,
        // и отсчёт «уезжает» относительно реального времени
        val deadline = SystemClock.elapsedRealtime() + timeLeft * 1000L

        // Таймер дольше 10 минут: без перезахвата WakeLock истекал и устройство засыпало
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock?.acquire((timeLeft + 60) * 1000L)

        val pendingIntent = openAppIntent()

        startForeground(NOTIFICATION_ID, buildProgressNotification(formatTime(timeLeft), pendingIntent))

        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            var shownSeconds = timeLeft
            while (true) {
                val leftMillis = deadline - SystemClock.elapsedRealtime()
                if (leftMillis <= 0) break
                val seconds = ((leftMillis + 999) / 1000).toInt()
                if (seconds != shownSeconds) {
                    shownSeconds = seconds
                    NotificationManagerCompat.from(this@TimerForeground)
                        .notify(NOTIFICATION_ID, buildProgressNotification(formatTime(seconds), pendingIntent))
                }
                // Спим до смены секунды, но не дольше — чтобы пережить задержки планировщика и не отставать
                delay(minOf(leftMillis, 1000L))
            }

            playRingtone()
            stopForeground(STOP_FOREGROUND_REMOVE)
            notifyFinished()
            stopSelf()
        }

        return START_STICKY
    }

    private suspend fun playRingtone() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
            delay(15000L)
            ringtone.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun notifyFinished() {
        // Отдельное уведомление поверх прогресса: оно должно остаться в шторке и разбудить звуком
        val notification = NotificationCompat.Builder(this, DONE_CHANNEL_ID)
            .setContentTitle("ShellClock")
            .setContentText("Таймер завершён")
            .setSmallIcon(R.drawable.ic_launcher_s)
            .setContentIntent(openAppIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(DONE_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS не выдан — звук всё равно был
        }
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun formatTime(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return when {
            h > 0 -> String.format("Осталось: %dч:%02dм:%02dс", h, m, s)
            m > 0 -> String.format("Осталось: %dм:%02dс", m, s)
            else -> String.format("Осталось: %dс", s)
        }
    }

    private fun buildProgressNotification(text: String, pendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShellClock")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_s)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progressChannel = NotificationChannel(
                CHANNEL_ID,
                "Таймер ShellClock",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(progressChannel)

            val doneChannel = NotificationChannel(
                DONE_CHANNEL_ID,
                "Завершение таймера",
                NotificationManager.IMPORTANCE_HIGH
            )
            doneChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            manager.createNotificationChannel(doneChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        if (wakeLock?.isHeld == true) {
            try { wakeLock?.release() } catch (e: Exception) { }
        }
    }
}
