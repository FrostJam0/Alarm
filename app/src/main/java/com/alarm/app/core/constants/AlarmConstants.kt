package com.alarm.app.core.constants

/**
 * A central object that holds all constant values used throughout the Alarm application.
 *
 * This includes bitmask values for days of the week, Intent extra keys, notification
 * identifiers, and channel identifiers used for alarm triggering and management.
 */
object AlarmConstants {
    // Bitmask values for days
    const val DAY_SUNDAY = 0b0000001
    const val DAY_MONDAY = 0b0000010
    const val DAY_TUESDAY = 0b0000100
    const val DAY_WEDNESDAY = 0b0001000
    const val DAY_THURSDAY = 0b0010000
    const val DAY_FRIDAY = 0b0100000
    const val DAY_SATURDAY = 0b1000000

    /** Key used to pass the alarm ID as an extra in Intents. */
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    
    /** Unique identifier for the foreground service notification when an alarm rings. */
    const val NOTIFICATION_ID_ALARM = 1001
    
    /** The ID of the notification channel used for alarm notifications. */
    const val NOTIFICATION_CHANNEL_ID = "alarm_channel"
}
