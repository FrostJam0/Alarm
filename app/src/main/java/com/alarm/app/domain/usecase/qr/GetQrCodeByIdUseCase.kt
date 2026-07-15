package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import javax.inject.Inject

/**
 * Use case for retrieving a specific QR code by its unique identifier.
 * 
 * Used when detailed information about a single QR code is needed, such as when viewing 
 * its properties, attempting to re-export a generated code, or resolving an alarm's linked QR code.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class GetQrCodeByIdUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Executes the use case to fetch a QR code.
     * 
     * @param id The unique identifier of the QR code to retrieve.
     * @return The [QrCode] matching the given ID, or null if it does not exist.
     */
    suspend operator fun invoke(id: Int): QrCode? {
        return qrCodeRepository.getQrCodeById(id)
    }
}
