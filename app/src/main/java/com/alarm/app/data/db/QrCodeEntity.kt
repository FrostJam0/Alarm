package com.alarm.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a database entity for a QR Code.
 * Stored in the "qr_codes" table.
 * Contains information about the QR code's name, string value, and creation details.
 */
@Entity(
    tableName = "qr_codes",
    indices = [
        Index(value = ["value"], unique = true)
    ]
)
data class QrCodeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "is_generated")
    val isGenerated: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
