package com.alarm.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database class representing the main database for the Alarm application.
 * Manages DAOs for alarms and QR codes entities.
 */
@Database(entities = [AlarmEntity::class, QrCodeEntity::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun qrCodeDao(): QrCodeDao
}
