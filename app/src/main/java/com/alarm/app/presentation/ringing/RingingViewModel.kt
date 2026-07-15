package com.alarm.app.presentation.ringing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.core.constants.AlarmConstants
import com.alarm.app.data.datastore.AppPreferencesDataStore
import com.alarm.app.domain.usecase.alarm.GetAlarmByIdUseCase
import com.alarm.app.domain.usecase.alarm.ToggleAlarmUseCase
import androidx.camera.core.ImageAnalysis
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * ViewModel managing the state and logic for the active ringing alarm screen.
 *
 * This ViewModel retrieves the currently ringing alarm from the database, extracts
 * its expected QR code value, and verifies scanned QR codes against this expected value.
 * It also manages UI states for successful dismissal and scan mismatches.
 *
 * @property savedStateHandle Contains arguments passed to the destination, such as the alarm ID.
 * @property getAlarmByIdUseCase Use case to fetch the active alarm details.
 * @property toggleAlarmUseCase Use case to turn off one-time alarms once they ring.
 * @property dataStore The preferences data store for clearing the ringing state.
 */
@HiltViewModel
class RingingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val dataStore: AppPreferencesDataStore
) : ViewModel() {

    private val alarmId: Int = savedStateHandle.get<Int>(AlarmConstants.EXTRA_ALARM_ID) ?: -1

    private val _expectedQrValue = MutableStateFlow<String?>(null)
    val expectedQrValue: StateFlow<String?> = _expectedQrValue.asStateFlow()

    private val _alarmLabel = MutableStateFlow("ALARM")
    val alarmLabel: StateFlow<String> = _alarmLabel.asStateFlow()

    private val _dismissed = MutableStateFlow(false)
    val dismissed: StateFlow<Boolean> = _dismissed.asStateFlow()

    private val _scanMismatch = MutableStateFlow(false)
    val scanMismatch: StateFlow<Boolean> = _scanMismatch.asStateFlow()

    init {
        if (alarmId != -1) {
            viewModelScope.launch {
                val alarm = getAlarmByIdUseCase(alarmId)
                if (alarm != null) {
                    _expectedQrValue.value = alarm.qrCodeValue
                    _alarmLabel.value = if (alarm.label.isNotBlank()) alarm.label.uppercase() else "ALARM"
                    
                    // If one-time alarm, disable it
                    if (alarm.repeatDays == 0) {
                        toggleAlarmUseCase(alarmId, false)
                    }
                }
            }
        }
    }

    /**
     * Processes a newly scanned QR code value.
     *
     * If the scanned value matches the expected value, the dismissed state is triggered.
     * Otherwise, a mismatch error state is shown temporarily.
     *
     * @param scannedValue The decoded string from the camera's barcode scanner.
     */
    fun onQrScanned(scannedValue: String) {
        if (_expectedQrValue.value == null) return
        
        if (scannedValue == _expectedQrValue.value) {
            _dismissed.value = true
        } else {
            _scanMismatch.value = true
        }
    }

    /**
     * Clears the scan mismatch error state.
     */
    fun clearMismatch() {
        _scanMismatch.value = false
    }

    /**
     * Bypasses the QR scan and forces the alarm to dismiss.
     */
    fun forceDismiss() {
        _dismissed.value = true
    }

    /**
     * Clears the currently ringing alarm ID from the data store after dismissal is complete.
     */
    fun onDismissedComplete() {
        viewModelScope.launch {
            dataStore.setCurrentlyRingingAlarmId(null)
        }
    }

    val analyzerExecutor = Executors.newSingleThreadExecutor()

    fun getBarcodeAnalyzer(onScanned: (String) -> Unit): ImageAnalysis.Analyzer {
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        return ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { value ->
                            onScanned(value)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
}
