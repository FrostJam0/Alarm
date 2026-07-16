package com.alarm.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarm.app.core.constants.AlarmConstants
import com.alarm.app.core.util.WakeLockManager
import com.alarm.app.data.datastore.AppPreferencesDataStore
import com.alarm.app.domain.repository.AlarmRepository
import com.alarm.app.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A [BroadcastReceiver] triggered by the system's AlarmManager when an alarm goes off.
 *
 * This receiver acts as a bridge between the system's alarm trigger and the [AlarmService]
 * that actually handles ringing and vibrating. It acquires a wake lock to ensure the CPU
 * stays on while it determines if another alarm is already ringing and starts the service.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStore: AppPreferencesDataStore

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when the alarm intent is broadcast by the system.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received, containing the alarm ID.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmConstants.EXTRA_ALARM_ID, -1)
        if (alarmId == -1) return

        val pendingResult = goAsync()
        WakeLockManager.acquire(context)

        scope.launch {
            try {
                startAlarmService(context, alarmId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Starts the foreground [AlarmService] to handle the ringing of the alarm.
     *
     * @param context The Context in which the receiver is running.
     * @param alarmId The ID of the alarm that is going off.
     */
    private fun startAlarmService(context: Context, alarmId: Int) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
        }
        context.startForegroundService(serviceIntent)
    }
}
