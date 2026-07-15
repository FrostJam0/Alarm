package com.alarm.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alarm.app.core.constants.AlarmConstants
import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.usecase.alarm.GetNextTriggerTimeUseCase
import com.alarm.app.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responsible for scheduling and canceling system alarms using Android's [AlarmManager].
 *
 * This class translates application-level [Alarm] models into actual system-level
 * alarms. It uses [AlarmManager.setAlarmClock] to ensure that alarms trigger at the exact
 * specified time and display an alarm icon in the device status bar.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getNextTriggerTimeUseCase: GetNextTriggerTimeUseCase
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a system alarm for the specified [Alarm].
     *
     * If the alarm is disabled, this method will instead cancel any existing system alarm
     * associated with it. For enabled alarms, it calculates the next trigger time using
     * [GetNextTriggerTimeUseCase] and sets the alarm via the system's [AlarmManager].
     *
     * @param alarm The [Alarm] to be scheduled.
     */
    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancel(alarm.id)
            return
        }
        val triggerTime = getNextTriggerTimeUseCase(alarm.hour, alarm.minute, alarm.repeatDays)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Show info in status bar (e.g. alarm icon)
        val info = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
    }

    /**
     * Cancels an existing system alarm.
     *
     * This removes the scheduled intent from the [AlarmManager] so that it will no longer fire.
     *
     * @param alarmId The unique identifier of the alarm to cancel.
     */
    fun cancel(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
