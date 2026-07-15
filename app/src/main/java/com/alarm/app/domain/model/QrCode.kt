package com.alarm.app.domain.model

/**
 * Represents a registered QR code in the user's personal library within the domain layer.
 * 
 * A QrCode is maintained independently of any specific alarm, acting as a reusable physical key.
 * It can be linked to one or more alarms, requiring the user to scan the physical code to dismiss them.
 *
 * @property id Auto-generated primary key uniquely identifying the QR code.
 * @property name User-assigned label for the QR code, e.g. "Bathroom mirror", to help identify its physical location.
 * @property value The raw string data encoded in the QR code. This is immutable after registration.
 * @property isGenerated Boolean flag indicating if the app generated this code internally (allowing re-export) or if it was registered by scanning an external code.
 * @property createdAt Epoch timestamp in milliseconds recording when this QR code was registered.
 */
data class QrCode(
    val id: Int,
    val name: String,
    val value: String,
    val isGenerated: Boolean,
    val createdAt: Long
)
