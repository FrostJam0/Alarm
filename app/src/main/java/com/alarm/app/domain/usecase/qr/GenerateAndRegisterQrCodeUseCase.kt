package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for dynamically generating and registering a brand new QR code.
 * 
 * Allows users who don't have an existing physical QR code to generate one within the app, 
 * which they can then export and print. It creates a unique UUID to serve as the secure 
 * underlying payload for the code.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class GenerateAndRegisterQrCodeUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Generates a new unique QR code and persists it.
     * 
     * Logic:
     * 1. Generates a secure random UUID prefixed with "alarm-".
     * 2. Constructs a new [QrCode] entity marked as `isGenerated = true`.
     * 3. Saves it to the database via the repository.
     * 
     * @param name A user-friendly name for this generated QR code (e.g., "Printable Bedside Code").
     * @return The unique ID of the newly registered QR code.
     */
    suspend operator fun invoke(name: String): Long {
        val uuidValue = "alarm-${UUID.randomUUID()}"
        val qrCode = QrCode(
            id = 0,
            name = name,
            value = uuidValue,
            isGenerated = true,
            createdAt = System.currentTimeMillis()
        )
        return qrCodeRepository.registerQrCode(qrCode)
    }
}
