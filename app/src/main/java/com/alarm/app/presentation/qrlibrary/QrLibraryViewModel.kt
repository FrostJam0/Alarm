package com.alarm.app.presentation.qrlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.domain.model.QrCode
import com.alarm.app.domain.usecase.qr.DeleteQrCodeUseCase
import com.alarm.app.domain.usecase.qr.GenerateAndRegisterQrCodeUseCase
import com.alarm.app.domain.usecase.qr.GetAllQrCodesUseCase
import com.alarm.app.domain.usecase.qr.RegisterScannedQrCodeUseCase
import com.alarm.app.domain.usecase.qr.RenameQrCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.alarm.app.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.combine

/**
 * UI state representing a single QR code and its associated metadata in the library.
 *
 * @property qrCode The underlying QR code data.
 * @property usageCount The number of alarms currently using this QR code.
 */
data class QrCodeItemUiState(
    val qrCode: QrCode,
    val usageCount: Int,
    val alarmLabels: List<String> = emptyList()
)

/**
 * The complete UI state for the QR Library screen.
 *
 * @property qrCodes The list of QR codes available in the library.
 * @property isLoading Indicates whether the initial data is still being loaded.
 */
data class QrLibraryUiState(
    val qrCodes: List<QrCodeItemUiState> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel responsible for managing the QR code library.
 *
 * This ViewModel handles loading the list of QR codes, calculating their usage counts across alarms,
 * and processing user actions such as registering, generating, renaming, and deleting QR codes.
 *
 * @property getAllQrCodesUseCase Use case for retrieving all saved QR codes.
 * @property registerScannedQrCodeUseCase Use case for registering a newly scanned QR code.
 * @property generateAndRegisterQrCodeUseCase Use case for generating and saving a new QR code.
 * @property renameQrCodeUseCase Use case for changing the name of an existing QR code.
 * @property deleteQrCodeUseCase Use case for removing a QR code from the database.
 * @property getQrCodesUsedByAlarmsUseCase Use case for counting how many alarms use a specific QR code.
 */
@HiltViewModel
class QrLibraryViewModel @Inject constructor(
    private val getAllQrCodesUseCase: GetAllQrCodesUseCase,
    private val registerScannedQrCodeUseCase: RegisterScannedQrCodeUseCase,
    private val generateAndRegisterQrCodeUseCase: GenerateAndRegisterQrCodeUseCase,
    private val renameQrCodeUseCase: RenameQrCodeUseCase,
    private val deleteQrCodeUseCase: DeleteQrCodeUseCase,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrLibraryUiState())
    val uiState: StateFlow<QrLibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(getAllQrCodesUseCase(), alarmRepository.getAllAlarms()) { qrList, alarms ->
                qrList.map { qr ->
                    val linkedAlarms = alarms.filter { it.qrCodeId == qr.id }
                    QrCodeItemUiState(qr, linkedAlarms.size, linkedAlarms.map { it.label.ifBlank { "Alarm" } })
                }
            }.collectLatest { items ->
                _uiState.value = QrLibraryUiState(qrCodes = items, isLoading = false)
            }
        }
    }

    /**
     * Registers a scanned QR code with the given name and value.
     *
     * @param name The user-provided name for the scanned QR code.
     * @param value The decoded string value from the scanned QR code.
     * @param onResult Callback containing a boolean indicating success, and an optional error message.
     */
    fun registerScannedQrCode(name: String, value: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                registerScannedQrCodeUseCase(name, value)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /**
     * Generates a new unique QR code and registers it with the given name.
     *
     * @param name The user-provided name for the newly generated QR code.
     * @param onResult Callback containing a boolean indicating success, and an optional error message.
     */
    fun generateAndRegisterQrCode(name: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                generateAndRegisterQrCodeUseCase(name)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /**
     * Renames an existing QR code.
     *
     * @param id The unique identifier of the QR code to rename.
     * @param newName The new name to assign to the QR code.
     */
    fun renameQrCode(id: Int, newName: String) {
        viewModelScope.launch {
            renameQrCodeUseCase(id, newName)
        }
    }

    /**
     * Deletes a QR code from the local database.
     *
     * @param qrCode The QR code entity to be deleted.
     */
    fun deleteQrCode(qrCode: QrCode) {
        viewModelScope.launch {
            deleteQrCodeUseCase(qrCode)
        }
    }
}
