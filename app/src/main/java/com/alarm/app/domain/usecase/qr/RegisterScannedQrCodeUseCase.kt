package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import javax.inject.Inject

/**
 * Use case for registering an externally scanned physical QR code into the user's library.
 * 
 * Captures the raw string data from a camera scan, validates it to prevent duplicates, 
 * and saves it so it can be assigned to future alarms.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class RegisterScannedQrCodeUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Validates and registers a newly scanned QR code.
     * 
     * Logic:
     * 1. Checks if a QR code with the exact same payload value already exists in the repository.
     * 2. If it does, throws an [IllegalArgumentException] to prevent duplicates.
     * 3. Otherwise, creates a new [QrCode] marked as `isGenerated = false` and persists it.
     * 
     * @param name The user-assigned name for this newly scanned code (e.g. "Kitchen Sink").
     * @param value The raw string payload decoded from the camera scan.
     * @return The unique ID of the newly registered QR code.
     * @throws IllegalArgumentException if the QR code payload is already registered.
     */
    suspend operator fun invoke(name: String, value: String): Long {
        val existing = qrCodeRepository.getQrCodeByValue(value)
        if (existing != null) {
            throw IllegalArgumentException("QR code already registered as '${existing.name}'")
        }
        val qrCode = QrCode(
            id = 0,
            name = name,
            value = value,
            isGenerated = false,
            createdAt = System.currentTimeMillis()
        )
        return qrCodeRepository.registerQrCode(qrCode)
    }
}
