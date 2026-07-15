package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import javax.inject.Inject

/**
 * Use case for deleting an existing QR code.
 * 
 * Encapsulates the logic to remove a registered QR code from the user's library.
 * It's generally expected that the UI or a higher layer verifies that the QR code is not 
 * currently in use by active alarms before calling this (using [GetQrCodesUsedByAlarmsUseCase]).
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class DeleteQrCodeUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Executes the use case to delete a QR code.
     * 
     * @param qrCode The [QrCode] entity to be removed from the system.
     */
    suspend operator fun invoke(qrCode: QrCode) {
        qrCodeRepository.deleteQrCode(qrCode)
    }
}
