package com.alarm.app.domain.usecase.alarm

import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for calculating the next exact timestamp when an alarm should trigger.
 * 
 * This calculates the nearest future timestamp for the alarm, handling edge cases 
 * like alarms set for earlier in the current day.
 */
class GetNextTriggerTimeUseCase @Inject constructor() {
    
    /**
     * Calculates the next trigger time in milliseconds since the epoch.
     * 
     * Logic:
     * 1. Constructs a target time for today using the given `hour` and `minute`.
     * 2. Checks if the target time has already passed today. If so, it shifts the trigger time to tomorrow.
     * 3. Alarms are treated as one-time, daily manual triggers.
     * 
     * @param hour The 24-hour format hour (0-23) the alarm is scheduled for.
     * @param minute The minute (0-59) the alarm is scheduled for.
     * @param repeatDays A bitmask representing the days of the week to repeat on (0 means no repeat).
     * @return The precise timestamp in milliseconds when the alarm should fire next.
     */
    operator fun invoke(hour: Int, minute: Int, repeatDays: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Always act as a one-time alarm
        if (next.before(now) || next.timeInMillis == now.timeInMillis) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
