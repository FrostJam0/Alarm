package com.alarm.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.alarm.app.R
import com.alarm.app.core.constants.AlarmConstants
import com.alarm.app.core.util.WakeLockManager
import com.alarm.app.domain.usecase.alarm.GetAlarmByIdUseCase
import com.alarm.app.presentation.ringing.RingingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A foreground [Service] responsible for handling an actively ringing alarm.
 *
 * This service plays the alarm audio, triggers the device vibrator, and displays
 * a high-priority full-screen intent notification to wake up the device and show
 * the [RingingActivity] to the user.
 */
@AndroidEntryPoint
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibratorManager: VibratorManager? = null
    @Suppress("DEPRECATION")
    private var screenWakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var getAlarmByIdUseCase: GetAlarmByIdUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called by the system when the service is first created.
     * Initializes the notification channel required for foreground execution.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService().
     *
     * Extracts the alarm ID, builds and posts the foreground notification, and starts playing
     * the audio and vibration.
     *
     * @param intent The Intent supplied to startService(), containing the alarm ID and optional ringtone URI.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return [START_STICKY] to ensure the service is restarted if killed by the system,
     *         or [START_NOT_STICKY] if invalid data is provided.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(AlarmConstants.EXTRA_ALARM_ID, -1) ?: -1
        if (alarmId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        wakeScreen()
        startForeground(AlarmConstants.NOTIFICATION_ID_ALARM, createNotification(alarmId))
        launchRingingActivity(alarmId)
        
        scope.launch {
            val alarm = getAlarmByIdUseCase(alarmId)
            playAlarmAudio(alarm?.ringtoneUri)
        }
        
        startVibration()

        return START_STICKY
    }

    /**
     * Directly launches the [RingingActivity] from the service.
     *
     * This bypasses the notification's fullScreenIntent entirely, so the alarm UI
     * appears regardless of the user's lock screen notification settings.
     * Requires SYSTEM_ALERT_WINDOW permission to start an activity from a service.
     */
    private fun launchRingingActivity(alarmId: Int) {
        val activityIntent = Intent(this, RingingActivity::class.java).apply {
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(activityIntent)
    }

    /**
     * Forces the device screen to turn on using a deprecated but functional
     * SCREEN_BRIGHT_WAKE_LOCK with ACQUIRE_CAUSES_WAKEUP.
     *
     * On Android 15 / ColorOS 15, the full-screen intent may silently fail to
     * launch the RingingActivity when the device is in deep sleep. Google Clock
     * bypasses this because it is a system-signed app. For third-party alarm apps,
     * this wake lock is the only reliable way to physically force the screen on
     * before the notification fires.
     */
    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            screenWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "AlarmApp::ScreenWake"
            )
            screenWakeLock?.acquire(60 * 1000L /* 1 minute */)
        }
    }

    /**
     * Creates the notification channel required for displaying the alarm notification on Android O and above.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AlarmConstants.NOTIFICATION_CHANNEL_ID,
            "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setBypassDnd(true)
            description = "Channel for ringing alarms"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    /**
     * Creates the high-priority foreground notification that includes a full-screen intent.
     *
     * @param alarmId The ID of the currently ringing alarm.
     * @return The constructed [Notification] instance.
     */
    private fun createNotification(alarmId: Int): Notification {
        val fullScreenIntent = Intent(this, RingingActivity::class.java).apply {
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AlarmConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm")
            .setContentText("Tap to dismiss")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .build()
    }

    /**
     * Starts playing the alarm audio using a [MediaPlayer].
     *
     * @param ringtoneUriStr The string URI of the custom ringtone, or null to use the default alarm sound.
     */
    private fun playAlarmAudio(ringtoneUriStr: String?) {
        try {
            val uri = if (ringtoneUriStr != null) {
                Uri.parse(ringtoneUriStr)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Starts device vibration with a continuous repeating pattern.
     */
    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            val timings = longArrayOf(0, 500, 500)
            val effect = VibrationEffect.createWaveform(timings, 1) // repeat from index 1
            vibrator?.vibrate(effect)
        }
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     *
     * Cleans up system resources: stops and releases the [MediaPlayer], cancels vibration,
     * and releases the CPU wake lock.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager?.defaultVibrator?.cancel()
        }

        if (screenWakeLock?.isHeld == true) {
            screenWakeLock?.release()
        }
        screenWakeLock = null
        
        WakeLockManager.release()
    }

    /**
     * Return the communication channel to the service. Since this service is not designed
     * for binding, it always returns null.
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
