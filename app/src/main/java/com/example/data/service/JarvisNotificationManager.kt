package com.example.data.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.ReminderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JarvisAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val title = intent.getStringExtra("title") ?: "J.A.R.V.I.S. Alarm"
        val isTimer = intent.getBooleanExtra("is_timer", false)

        if (action == JarvisNotificationManager.ACTION_STOP_ALARM) {
            JarvisNotificationManager.getInstance(context).stopAlarm()
            return
        }

        if (isTimer) {
            JarvisNotificationManager.getInstance(context).triggerTimerAlarm(title)
        } else {
            JarvisNotificationManager.getInstance(context).showReminderNotification(title)
        }
    }
}

class JarvisNotificationManager private constructor(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var alarmMediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val _isAlarmRinging = MutableStateFlow<String?>(null)
    val isAlarmRinging: StateFlow<String?> = _isAlarmRinging.asStateFlow()

    companion object {
        const val CHANNEL_REMINDERS = "jarvis_reminders_channel"
        const val CHANNEL_ALARMS = "jarvis_alarms_channel"
        const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"

        @Volatile
        private var instance: JarvisNotificationManager? = null

        fun getInstance(context: Context): JarvisNotificationManager {
            return instance ?: synchronized(this) {
                instance ?: JarvisNotificationManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannels()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "J.A.R.V.I.S. Erinnerungen & Termine",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Erinnerungen und anstehende Termine"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "J.A.R.V.I.S. Timer & Alarme",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Akustische Alarme bei Ablauf von Timern"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun showReminderNotification(title: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ J.A.R.V.I.S. Erinnerung")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    fun scheduleCalendarEvent(title: String, date: String, time: String) {
        val dateTimeStr = "$date $time"
        val targetMillis = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.parse(dateTimeStr)?.time ?: (System.currentTimeMillis() + 60000)
        } catch (e: Exception) {
            System.currentTimeMillis() + 60000
        }

        if (targetMillis <= System.currentTimeMillis()) {
            showReminderNotification("Termin: $title ($time Uhr)")
            return
        }

        val intent = Intent(context, JarvisAlarmReceiver::class.java).apply {
            putExtra("title", "Termin: $title ($time Uhr)")
            putExtra("is_timer", false)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dateTimeStr.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("JarvisNotification", "Error scheduling calendar alarm: ${e.message}")
        }
    }

    fun scheduleReminder(reminder: ReminderItem) {
        val targetMillis = parseDueTimeToMillis(reminder.dueTime)
        if (targetMillis <= System.currentTimeMillis()) {
            showReminderNotification(reminder.title)
            return
        }

        val intent = Intent(context, JarvisAlarmReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("is_timer", false)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("JarvisNotification", "Error scheduling alarm: ${e.message}")
        }
    }

    fun triggerTimerAlarm(label: String) {
        _isAlarmRinging.value = label

        // Play loud alarm sound
        try {
            stopAlarmSound()
            val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            alarmMediaPlayer = MediaPlayer().apply {
                setDataSource(context, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("JarvisNotification", "Error playing alarm sound: ${e.message}")
        }

        // Start vibration pattern
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 300, 500, 300, 1000),
                        0 // loop from index 0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 300, 500, 300, 1000), 0)
            }
        } catch (e: Exception) {
            // ignore vibration error
        }

        // Show Heads-Up Notification with "ALARM STOPPEN" button
        val stopIntent = Intent(context, JarvisAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            10001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALARMS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏱️ TIMER ABGELAUFEN!")
            .setContentText("J.A.R.V.I.S. Alarm: $label")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(launchPendingIntent, true)
            .setContentIntent(launchPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "TIMER BEENDEN", stopPendingIntent)
            .build()

        notificationManager.notify(8888, notification)
    }

    fun stopAlarm() {
        _isAlarmRinging.value = null
        stopAlarmSound()
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // ignore
        }
        notificationManager.cancel(8888)
    }

    private fun stopAlarmSound() {
        try {
            if (alarmMediaPlayer?.isPlaying == true) {
                alarmMediaPlayer?.stop()
            }
            alarmMediaPlayer?.release()
            alarmMediaPlayer = null
        } catch (e: Exception) {
            Log.e("JarvisNotification", "Error stopping alarm: ${e.message}")
        }
    }

    private fun parseDueTimeToMillis(dueTime: String): Long {
        return try {
            val now = Calendar.getInstance()
            val clean = dueTime.trim()

            // If time only like "14:30"
            if (clean.matches(Regex("""\d{1,2}:\d{2}"""))) {
                val parts = clean.split(":")
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    set(Calendar.MINUTE, parts[1].toInt())
                    set(Calendar.SECOND, 0)
                }
                if (cal.timeInMillis <= now.timeInMillis) {
                    // scheduled for tomorrow
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.timeInMillis
            } else if (clean.matches(Regex("""\d{4}-\d{2}-\d{2}\s+\d{1,2}:\d{2}"""))) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                sdf.parse(clean)?.time ?: (System.currentTimeMillis() + 60000)
            } else {
                // Default: 10 minutes from now
                System.currentTimeMillis() + 10 * 60 * 1000
            }
        } catch (e: Exception) {
            System.currentTimeMillis() + 60000
        }
    }
}
