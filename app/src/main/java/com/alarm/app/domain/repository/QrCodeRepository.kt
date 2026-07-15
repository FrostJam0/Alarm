package com.alarm.app.domain.repository

import com.alarm.app.domain.model.QrCode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [QrCode] entities.
 * 
 * Defines the contract for data operations related to QR codes, abstracting the underlying data source.
 * It manages the user's library of reusable QR codes that can be linked to alarms.
 */
interface QrCodeRepository {
    /**
     * Retrieves a reactive stream of all registered QR codes.
     * 
     * @return A [Flow] emitting a list of all [QrCode]s. The flow updates automatically upon data changes.
     */
    fun getAllQrCodes(): Flow<List<QrCode>>

    /**
     * Retrieves a specific QR code by its unique identifier.
     * 
     * @param id The unique identifier of the QR code.
     * @return The [QrCode] with the specified ID, or null if no such QR code exists.
     */
    suspend fun getQrCodeById(id: Int): QrCode?

    /**
     * Retrieves a specific QR code by its raw encoded value.
     * 
     * Used to verify if a newly scanned QR code is already registered in the system.
     * 
     * @param value The raw string value encoded in the QR code.
     * @return The [QrCode] matching the value, or null if not found.
     */
    suspend fun getQrCodeByValue(value: String): QrCode?

    /**
     * Registers a new QR code in the repository.
     * 
     * @param qrCode The [QrCode] instance to be saved.
     * @return The unique identifier (ID) assigned to the newly registered QR code.
     */
    suspend fun registerQrCode(qrCode: QrCode): Long

    /**
     * Renames an existing QR code.
     * 
     * @param id The unique identifier of the QR code to rename.
     * @param name The new user-assigned label for the QR code.
     */
    suspend fun renameQrCode(id: Int, name: String)

    /**
     * Deletes a QR code from the repository.
     * 
     * @param qrCode The [QrCode] instance to be removed.
     */
    suspend fun deleteQrCode(qrCode: QrCode)

    /**
     * Retrieves the count of alarms that are currently linked to a specific QR code.
     * 
     * This is useful for preventing the deletion of a QR code that is still actively used by an alarm.
     * 
     * @param qrCodeId The unique identifier of the QR code.
     * @return The number of alarms associated with the given QR code.
     */
    suspend fun getAlarmCountForQrCode(qrCodeId: Int): Int
}
