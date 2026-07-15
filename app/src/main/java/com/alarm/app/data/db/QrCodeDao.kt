package com.alarm.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing QR code data in the Room database.
 * Provides methods for querying, inserting, updating, and deleting QR codes.
 */
@Dao
interface QrCodeDao {
    /**
     * Retrieves a continuous flow of all stored QR codes, ordered by creation time descending.
     *
     * @return A Flow emitting a list of all [QrCodeEntity] objects.
     */
    @Query("SELECT * FROM qr_codes ORDER BY created_at DESC")
    fun getAllQrCodes(): Flow<List<QrCodeEntity>>

    /**
     * Fetches a single QR code by its unique identifier.
     *
     * @param id The unique ID of the QR code.
     * @return The [QrCodeEntity] if found, or null otherwise.
     */
    @Query("SELECT * FROM qr_codes WHERE id = :id")
    suspend fun getQrCodeById(id: Int): QrCodeEntity?

    /**
     * Fetches a single QR code by its string value.
     *
     * @param value The scanned or generated string value of the QR code.
     * @return The [QrCodeEntity] if found, or null otherwise.
     */
    @Query("SELECT * FROM qr_codes WHERE value = :value LIMIT 1")
    suspend fun getQrCodeByValue(value: String): QrCodeEntity?

    /**
     * Inserts a new QR code into the database. Aborts if a conflict (e.g. unique constraint) occurs.
     *
     * @param qrCode The [QrCodeEntity] to insert.
     * @return The row ID of the newly inserted QR code.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertQrCode(qrCode: QrCodeEntity): Long

    /**
     * Updates the name/label of a specific QR code.
     *
     * @param id The ID of the QR code to rename.
     * @param name The new name to assign.
     */
    @Query("UPDATE qr_codes SET name = :name WHERE id = :id")
    suspend fun renameQrCode(id: Int, name: String)

    /**
     * Deletes a specific QR code from the database.
     *
     * @param qrCode The [QrCodeEntity] to delete.
     */
    @Delete
    suspend fun deleteQrCode(qrCode: QrCodeEntity)

    /**
     * Counts the number of alarms currently associated with a given QR code.
     *
     * @param qrCodeId The ID of the QR code.
     * @return The number of alarms linked to this QR code.
     */
    @Query("SELECT COUNT(*) FROM alarms WHERE qr_code_id = :qrCodeId")
    suspend fun getAlarmCountForQrCode(qrCodeId: Int): Int
}
