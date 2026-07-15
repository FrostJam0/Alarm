package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.repository.QrCodeRepository
import javax.inject.Inject

/**
 * Use case for renaming an existing QR code.
 * 
 * Allows users to update the display name of a QR code in their library if they move the physical code
 * to a new location or want a more descriptive label.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class RenameQrCodeUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Executes the use case to rename a QR code.
     * 
     * @param id The unique identifier of the QR code to modify.
     * @param name The new, updated name for the QR code.
     */
    suspend operator fun invoke(id: Int, name: String) {
        qrCodeRepository.renameQrCode(id, name)
    }
}
