package com.alarm.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a database entity for an Alarm.
 * Stored in the "alarms" table.
 * Includes fields for schedule time, repeat days, label, associated QR code, and ringtone.
 */
@Entity(
    tableName = "alarms",
    indices = [
        Index(value = ["is_enabled"]),
        Index(value = ["repeat_days"]),
        Index(value = ["qr_code_id"])
    ]
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "hour")           val hour: Int,
    @ColumnInfo(name = "minute")         val minute: Int,
    @ColumnInfo(name = "label")          val label: String,
    @ColumnInfo(name = "is_enabled")     val isEnabled: Boolean,
    @ColumnInfo(name = "repeat_days")    val repeatDays: Int,
    @ColumnInfo(name = "qr_code_id")     val qrCodeId: Int,
    @ColumnInfo(name = "qr_code_value")  val qrCodeValue: String,
    @ColumnInfo(name = "ringtone_uri")   val ringtoneUri: String? = null,
    @ColumnInfo(name = "created_at")     val createdAt: Long,
    @ColumnInfo(name = "updated_at")     val updatedAt: Long
)
