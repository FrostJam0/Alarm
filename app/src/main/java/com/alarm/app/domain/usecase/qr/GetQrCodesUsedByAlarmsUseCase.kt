package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.repository.QrCodeRepository
import javax.inject.Inject

/**
 * Use case for checking how many alarms are actively relying on a specific QR code.
 * 
 * This is crucial for maintaining referential integrity in the UI workflow. 
 * Before a user deletes a QR code, the app uses this case to warn them if the code is still attached to alarms.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class GetQrCodesUsedByAlarmsUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Executes the use case to count dependent alarms.
     * 
     * @param qrCodeId The unique identifier of the QR code in question.
     * @return The number of alarms that have their `qrCodeId` set to this QR code.
     */
    suspend operator fun invoke(qrCodeId: Int): Int {
        return qrCodeRepository.getAlarmCountForQrCode(qrCodeId)
    }
}
