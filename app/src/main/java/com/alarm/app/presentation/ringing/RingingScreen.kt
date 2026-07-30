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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as CanvasSize
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.Dp

@Composable
fun RingingScreen(
    viewModel: RingingViewModel,
    onDismiss: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Block the back button — user must scan QR or tap 30 times to dismiss
    BackHandler(enabled = true) { /* no-op */ }
    
    val isDismissed by viewModel.dismissed.collectAsState()
    val alarmLabel by viewModel.alarmLabel.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanStatusText by viewModel.scanStatusText.collectAsState()

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            onDismiss()
        }
    }

    val isMismatch = scanState == ScanState.MISMATCH_FLASH

    LaunchedEffect(isMismatch) {
        if (isMismatch) {
            delay(500)
            viewModel.clearMismatch()
        }
    }

    val greenRegionBorderColor by animateColorAsState(
        targetValue = if (isMismatch) Color.Red.copy(alpha = 0.8f) else Color.Green.copy(alpha = 0.4f),
        label = "MismatchColorAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)) // Darker overlay
    ) {
        // Purple Region (10% height): Alarm Label
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.10f)
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFBAC3FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = alarmLabel,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }

        // Green Region (55% height): QR Scanner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.61f) // 0.61 of remaining 90% is roughly 55% of total
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, greenRegionBorderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (scanState == ScanState.IDLE) {
                Text(
                    text = "Tap to start QR Scanner",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        viewModel.startCamera()
                    }
                )
            } else {
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
                                        viewModel.getBarcodeAnalyzer()
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

                // Progress Ring
                if (scanState == ScanState.HOLDING || scanState == ScanState.GRACE) {
                    val ringColor = if (scanState == ScanState.GRACE) Color.Yellow.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
                    Canvas(modifier = Modifier.size(60.dp)) {
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * scanProgress,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Status Text
                if (scanStatusText.isNotEmpty()) {
                    Text(
                        text = scanStatusText,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Blue Region (35% height): Tap to Dismiss
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight() // Takes remaining height (~35%)
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFBAC3FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            val clickCount by ActiveAlarmState.clickCount.collectAsState()
            val savedPercentX by ActiveAlarmState.buttonPercentX.collectAsState()
            val savedPercentY by ActiveAlarmState.buttonPercentY.collectAsState()

            val buttonSize = 80.dp

            val maxOffsetX = maxWidth - buttonSize
            val maxOffsetY = maxHeight - buttonSize

            var offsetX by remember(maxWidth, maxHeight) { 
                mutableStateOf(maxOffsetX * savedPercentX)
            }
            var offsetY by remember(maxWidth, maxHeight) { 
                mutableStateOf(maxOffsetY * savedPercentY)
            }

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(buttonSize)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                val currentCount = ActiveAlarmState.clickCount.value + 1
                                ActiveAlarmState.clickCount.value = currentCount
                                if (currentCount >= 40) {
                                    viewModel.forceDismiss()
                                } else {
                                    val maxOffsetXVal = maxOffsetX.value
                                    val maxOffsetYVal = maxOffsetY.value
                                    
                                    val minDistance = 0.35f * sqrt(maxWidth.value * maxWidth.value + maxHeight.value * maxHeight.value)
                                    val currentXVal = offsetX.value
                                    val currentYVal = offsetY.value
                                    
                                    var newX = currentXVal
                                    var newY = currentYVal
                                    
                                    if (currentCount == 1) {
                                        // First tap: Just go anywhere random
                                        newX = if (maxOffsetXVal > 0) (Math.random() * maxOffsetXVal).toFloat() else 0f
                                        newY = if (maxOffsetYVal > 0) (Math.random() * maxOffsetYVal).toFloat() else 0f
                                    } else {
                                        // Subsequent taps: Try to find a spot far away
                                        var found = false
                                        for (i in 0 until 50) {
                                            val testX = if (maxOffsetXVal > 0) (Math.random() * maxOffsetXVal).toFloat() else 0f
                                            val testY = if (maxOffsetYVal > 0) (Math.random() * maxOffsetYVal).toFloat() else 0f
                                            
                                            val dx = testX - currentXVal
                                            val dy = testY - currentYVal
                                            val dist = sqrt(dx * dx + dy * dy)
                                            
                                            if (dist >= minDistance) {
                                                newX = testX
                                                newY = testY
                                                found = true
                                                break
                                            }
                                        }
                                        
                                        if (!found) {
                                            // Fallback: Pick a completely random spot if we couldn't find a far one
                                            newX = if (maxOffsetXVal > 0) (Math.random() * maxOffsetXVal).toFloat() else 0f
                                            newY = if (maxOffsetYVal > 0) (Math.random() * maxOffsetYVal).toFloat() else 0f
                                        }
                                    }
                                    
                                    offsetX = newX.dp
                                    offsetY = newY.dp
                                    
                                    // Save as percentages for orientation independence
                                    val percentX = if (maxOffsetXVal > 0) newX / maxOffsetXVal else 0.5f
                                    val percentY = if (maxOffsetYVal > 0) newY / maxOffsetYVal else 0.5f
                                    ActiveAlarmState.buttonPercentX.value = percentX
                                    ActiveAlarmState.buttonPercentY.value = percentY
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to\nDismiss\n($clickCount/40)",
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
