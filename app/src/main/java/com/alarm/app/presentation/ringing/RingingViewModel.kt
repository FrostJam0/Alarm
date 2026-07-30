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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.util.concurrent.Executors
import javax.inject.Inject

enum class ScanState {
    IDLE,              // Camera not yet activated
    WAITING_FOR_QR,    // Camera active, no correct QR in view
    HOLDING,           // Correct QR visible, accumulating progress
    GRACE,             // QR temporarily lost, 1s grace before reset
    MISMATCH_FLASH,    // Wrong QR scanned, red flash
    DISMISSED          // 8s hold complete
}

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

    private val _scanState = MutableStateFlow(ScanState.IDLE)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("")
    val scanStatusText: StateFlow<String> = _scanStatusText.asStateFlow()

    private var _accumulatedMs = 0L
    private var _lastQrSeenTimestamp = 0L
    private var progressJob: Job? = null

    companion object {
        const val HOLD_DURATION_MS = 8000L
        const val GRACE_PERIOD_MS = 1000L
        const val MIN_QR_AREA_PERCENT = 15f
        const val SCAN_TICK_INTERVAL_MS = 50L
    }

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

    fun startCamera() {
        if (_scanState.value == ScanState.IDLE) {
            updateScanState(ScanState.WAITING_FOR_QR)
        }
    }

    private fun updateScanState(newState: ScanState) {
        _scanState.value = newState
        _scanStatusText.value = when (newState) {
            ScanState.IDLE -> ""
            ScanState.WAITING_FOR_QR -> "Point camera at your QR code"
            ScanState.HOLDING -> "Hold still..."
            ScanState.GRACE -> "Hold still..."
            ScanState.MISMATCH_FLASH -> "Wrong QR code!"
            ScanState.DISMISSED -> "✓ Alarm dismissed"
        }
    }

    fun onFrameAnalyzed(value: String?, areaPercent: Float) {
        if (_expectedQrValue.value == null || _scanState.value == ScanState.DISMISSED) return

        if (value == null || areaPercent < MIN_QR_AREA_PERCENT) {
            return
        }

        if (value == _expectedQrValue.value) {
            _lastQrSeenTimestamp = System.currentTimeMillis()
            if (_scanState.value == ScanState.WAITING_FOR_QR || _scanState.value == ScanState.GRACE || _scanState.value == ScanState.MISMATCH_FLASH) {
                updateScanState(ScanState.HOLDING)
                startProgressTicker()
            }
        } else {
            if (_scanState.value == ScanState.WAITING_FOR_QR) {
                updateScanState(ScanState.MISMATCH_FLASH)
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (_scanState.value == ScanState.HOLDING || _scanState.value == ScanState.GRACE) {
                delay(SCAN_TICK_INTERVAL_MS)
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now
                
                val timeSinceLastQr = now - _lastQrSeenTimestamp
                
                if (timeSinceLastQr > 300) {
                    if (_scanState.value == ScanState.HOLDING) {
                        updateScanState(ScanState.GRACE)
                    } else if (_scanState.value == ScanState.GRACE && timeSinceLastQr > GRACE_PERIOD_MS) {
                        _accumulatedMs = 0
                        _scanProgress.value = 0f
                        updateScanState(ScanState.WAITING_FOR_QR)
                        break
                    }
                } else {
                    if (_scanState.value == ScanState.GRACE) {
                        updateScanState(ScanState.HOLDING)
                    }
                    _accumulatedMs += delta
                    _scanProgress.value = _accumulatedMs.toFloat() / HOLD_DURATION_MS
                    if (_accumulatedMs >= HOLD_DURATION_MS) {
                        updateScanState(ScanState.DISMISSED)
                        _dismissed.value = true
                        break
                    }
                }
            }
        }
    }

    fun clearMismatch() {
        if (_scanState.value == ScanState.MISMATCH_FLASH) {
            updateScanState(ScanState.WAITING_FOR_QR)
        }
    }

    fun forceDismiss() {
        _dismissed.value = true
    }

    fun onDismissedComplete() {
        viewModelScope.launch {
            dataStore.setCurrentlyRingingAlarmId(null)
        }
    }

    val analyzerExecutor = Executors.newSingleThreadExecutor()

    fun getBarcodeAnalyzer(): ImageAnalysis.Analyzer {
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        return ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val rotation = imageProxy.imageInfo.rotationDegrees
                val frameWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
                val frameHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
                val frameArea = (frameWidth * frameHeight).toFloat()
                
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val barcode = barcodes.firstOrNull()
                        val value = barcode?.rawValue
                        var areaPercent = 0f
                        
                        val boundingBox = barcode?.boundingBox
                        if (boundingBox != null && frameArea > 0) {
                            val barcodeArea = boundingBox.width().toFloat() * boundingBox.height().toFloat()
                            areaPercent = (barcodeArea / frameArea) * 100f
                        }
                        
                        onFrameAnalyzed(value, areaPercent)
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
