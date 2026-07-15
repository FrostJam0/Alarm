package com.alarm.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarm.app.domain.repository.AlarmRepository
import com.alarm.app.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A [BroadcastReceiver] that listens for time or timezone changes on the device.
 *
 * When the user manually changes the device time or timezone, the previously scheduled
 * system alarms might fire at incorrect times. This receiver listens to these changes
 * and reschedules all enabled alarms based on the newly set time.
 */
@AndroidEntryPoint
class TimeChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when the system time or timezone has been changed.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received (TIME_SET or TIMEZONE_CHANGED).
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_CHANGED || 
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val enabledAlarms = alarmRepository.getEnabledAlarms()
                    enabledAlarms.forEach { alarm ->
                        alarmScheduler.schedule(alarm)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
