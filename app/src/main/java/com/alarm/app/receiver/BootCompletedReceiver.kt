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
 * A [BroadcastReceiver] that listens for device boot completion events.
 *
 * Since the system AlarmManager clears all scheduled alarms upon device reboot,
 * this receiver queries the database for all enabled alarms and reschedules them
 * automatically.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when the system finishes booting.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received (BOOT_COMPLETED).
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
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
