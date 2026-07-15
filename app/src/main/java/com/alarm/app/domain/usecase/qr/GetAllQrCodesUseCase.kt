package com.alarm.app.domain.usecase.qr

import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.repository.QrCodeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a reactive stream of all registered QR codes.
 * 
 * Provides a [Flow] that continuously emits the latest list of QR codes from the user's library.
 * This is primarily used for populating UI screens where the user manages or selects QR codes.
 *
 * @property qrCodeRepository The repository managing QR code data operations.
 */
class GetAllQrCodesUseCase @Inject constructor(
    private val qrCodeRepository: QrCodeRepository
) {
    /**
     * Executes the use case to fetch all QR codes as a Flow.
     * 
     * @return A [Flow] emitting a list of all [QrCode] entities.
     */
    operator fun invoke(): Flow<List<QrCode>> {
        return qrCodeRepository.getAllQrCodes()
    }
}
