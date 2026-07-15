package com.alarm.app.data.repository

import androidx.room.withTransaction
import com.alarm.app.data.db.AlarmDatabase
import com.alarm.app.data.db.QrCodeDao
import com.alarm.app.data.mapper.toDomain
import com.alarm.app.data.mapper.toEntity
import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of the [QrCodeRepository] interface.
 * Coordinates data operations for QR codes, handling interactions between
 * the data source (DAO) and the domain layer.
 */
class QrCodeRepositoryImpl @Inject constructor(
    private val database: AlarmDatabase,
    private val qrCodeDao: QrCodeDao
) : QrCodeRepository {

    /**
     * Retrieves a continuous flow of all QR codes, mapped to domain models.
     *
     * @return A Flow emitting a list of [QrCode] objects.
     */
    override fun getAllQrCodes(): Flow<List<QrCode>> {
        return qrCodeDao.getAllQrCodes().map { list -> list.map { it.toDomain() } }
    }

    /**
     * Fetches a single QR code by its ID.
     *
     * @param id The unique identifier of the QR code.
     * @return The [QrCode] domain model if found, null otherwise.
     */
    override suspend fun getQrCodeById(id: Int): QrCode? {
        return qrCodeDao.getQrCodeById(id)?.toDomain()
    }

    /**
     * Fetches a single QR code by its string value.
     *
     * @param value The string value of the QR code.
     * @return The [QrCode] domain model if found, null otherwise.
     */
    override suspend fun getQrCodeByValue(value: String): QrCode? {
        return qrCodeDao.getQrCodeByValue(value)?.toDomain()
    }

    /**
     * Registers a new QR code in the repository.
     *
     * @param qrCode The [QrCode] domain model to insert.
     * @return The ID of the newly registered QR code.
     */
    override suspend fun registerQrCode(qrCode: QrCode): Long {
        return qrCodeDao.insertQrCode(qrCode.toEntity())
    }

    /**
     * Renames an existing QR code.
     *
     * @param id The ID of the QR code to rename.
     * @param name The new name to assign.
     */
    override suspend fun renameQrCode(id: Int, name: String) {
        qrCodeDao.renameQrCode(id, name)
    }

    /**
     * Deletes a QR code and handles cascading effects,
     * such as resetting the associated qr_code_id on linked alarms.
     *
     * @param qrCode The [QrCode] domain model to delete.
     */
    override suspend fun deleteQrCode(qrCode: QrCode) {
        database.withTransaction {
            qrCodeDao.deleteQrCode(qrCode.toEntity())
            database.alarmDao().orphanAlarmsForQrCode(qrCode.id)
        }
    }

    /**
     * Returns the count of alarms associated with a specific QR code.
     *
     * @param qrCodeId The ID of the QR code.
     * @return The number of alarms linked to the given QR code.
     */
    override suspend fun getAlarmCountForQrCode(qrCodeId: Int): Int {
        return qrCodeDao.getAlarmCountForQrCode(qrCodeId)
    }
}
