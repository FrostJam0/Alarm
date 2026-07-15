package com.alarm.app.domain.model

/**
 * Represents an Alarm entity within the domain layer.
 * 
 * This model encapsulates all the business rules and data required to schedule and trigger an alarm.
 * It handles scheduling details like time (hour/minute), repeat days, and ties the alarm to a specific QR code for dismissal.
 * 
 * @property id Unique identifier for the alarm.
 * @property hour Hour of the day the alarm is set for (0-23).
 * @property minute Minute of the hour the alarm is set for (0-59).
 * @property label A user-defined name or label for the alarm.
 * @property isEnabled Whether the alarm is currently active and scheduled to trigger.
 * @property repeatDays A bitmask representing the days of the week the alarm should repeat. 0 indicates a one-time alarm.
 * @property qrCodeId The ID of the associated QR code required to dismiss this alarm. -1 indicates no QR code is linked.
 * @property qrCodeValue A resilient copy of the QR code's raw value, used for matching during the alarm ringing screen.
 * @property ringtoneUri The URI of the ringtone to play when the alarm triggers. If null, the system default ringtone is used.
 * @property createdAt Timestamp in milliseconds when the alarm was created.
 * @property updatedAt Timestamp in milliseconds when the alarm was last updated.
 */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val repeatDays: Int,
    val qrCodeId: Int,
    val qrCodeValue: String,
    val ringtoneUri: String?,
    val createdAt: Long,
    val updatedAt: Long
)
