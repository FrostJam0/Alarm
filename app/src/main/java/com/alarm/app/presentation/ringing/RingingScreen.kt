package com.alarm.app.presentation.ringing

import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay

/**
 * The main UI shown when an alarm is actively ringing.
 *
 * Displays a camera preview and prompts the user to scan the specific QR code
 * associated with the ringing alarm to dismiss it. Highlights the screen in red
 * if the user scans an incorrect QR code.
 *
 * @param viewModel The ViewModel providing the expected QR code value and handling scan logic.
 * @param onDismiss Callback invoked when the correct QR code is scanned and the alarm should be dismissed.
 */
@Composable
fun RingingScreen(
    viewModel: RingingViewModel,
    onDismiss: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val isDismissed by viewModel.dismissed.collectAsState()
    val isMismatch by viewModel.scanMismatch.collectAsState()

    val alarmLabel by viewModel.alarmLabel.collectAsState()

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            onDismiss()
        }
    }

    LaunchedEffect(isMismatch) {
        if (isMismatch) {
            delay(500)
            viewModel.clearMismatch()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isMismatch) Color.Red.copy(alpha = 0.5f) else Color.Black,
        animationSpec = infiniteRepeatable(
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MismatchColorAnimation"
    )

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val totalMs = 20000f
            val stepMs = 50f
            var elapsed = 0f
            while (elapsed < totalMs) {
                delay(stepMs.toLong())
                elapsed += stepMs
                holdProgress = elapsed / totalMs
            }
            viewModel.forceDismiss()
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isMismatch) backgroundColor else Color.Black)
    ) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                viewModel.analyzerExecutor,
                                viewModel.getBarcodeAnalyzer { value ->
                                    viewModel.onQrScanned(value)
                                }
                            )
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Log.e("RingingScreen", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = alarmLabel,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
        }

        // Emergency Hold Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isHolding) {
                CircularProgressIndicator(
                    progress = { holdProgress },
                    color = Color.Red,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = "Hold to\nForce Dismiss",
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
