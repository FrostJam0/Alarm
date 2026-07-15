package com.alarm.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing alarm data in the Room database.
 * Provides methods for querying, inserting, updating, and deleting alarms.
 */
@Dao
interface AlarmDao {
    /**
     * Retrieves a continuous flow of all alarms ordered by time (hour, then minute).
     *
     * @return A Flow emitting a list of all [AlarmEntity] objects.
     */
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /**
     * Fetches a single alarm by its unique identifier.
     *
     * @param id The unique ID of the alarm.
     * @return The [AlarmEntity] if found, or null otherwise.
     */
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    /**
     * Retrieves a list of all currently enabled alarms.
     *
     * @return A list of enabled [AlarmEntity] objects.
     */
    @Query("SELECT * FROM alarms WHERE is_enabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    /**
     * Inserts a new alarm or replaces an existing one if a conflict occurs.
     *
     * @param alarm The [AlarmEntity] to insert.
     * @return The row ID of the newly inserted alarm.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    /**
     * Updates an existing alarm's properties.
     *
     * @param alarm The [AlarmEntity] to update.
     */
    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    /**
     * Deletes a specific alarm from the database.
     *
     * @param alarm The [AlarmEntity] to delete.
     */
    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    /**
     * Updates only the enabled state and modification timestamp of a specific alarm.
     *
     * @param id The ID of the alarm to update.
     * @param enabled The new enabled state.
     * @param updatedAt The timestamp of this update.
     */
    @Query("UPDATE alarms SET is_enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean, updatedAt: Long)

    /**
     * Disassociates alarms from a deleted QR code by resetting their qr_code_id to -1.
     *
     * @param deletedQrCodeId The ID of the QR code that was deleted.
     */
    @Query("UPDATE alarms SET qr_code_id = -1 WHERE qr_code_id = :deletedQrCodeId")
    suspend fun orphanAlarmsForQrCode(deletedQrCodeId: Int)
}
