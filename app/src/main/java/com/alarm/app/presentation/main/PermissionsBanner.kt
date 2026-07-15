package com.alarm.app.presentation.main

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsBanner(
    modifier: Modifier = Modifier,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val permissionsToRequest = mutableListOf(Manifest.permission.CAMERA)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionState = rememberMultiplePermissionsState(permissionsToRequest)
    
    var exactAlarmGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
        )
    }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    var canDrawOverlays by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var canUseFullScreenIntent by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) notificationManager.canUseFullScreenIntent() else true
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
                isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                canDrawOverlays = Settings.canDrawOverlays(context)
                canUseFullScreenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) notificationManager.canUseFullScreenIntent() else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cameraGranted = permissionState.permissions.find { it.permission == Manifest.permission.CAMERA }?.status?.isGranted == true

    LaunchedEffect(permissionState.allPermissionsGranted, exactAlarmGranted, isIgnoringBatteryOptimizations, canDrawOverlays, canUseFullScreenIntent) {
        if (permissionState.allPermissionsGranted && exactAlarmGranted && isIgnoringBatteryOptimizations && canDrawOverlays && canUseFullScreenIntent) {
            onAllPermissionsGranted()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!permissionState.allPermissionsGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (!cameraGranted) "Camera permission is required to dismiss alarms." else "Notifications are required to show alarms.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = { permissionState.launchMultiplePermissionRequest() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Grant Permissions")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Exact alarm permission is needed to fire alarms precisely on time.",
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Grant Exact Alarm")
                }
            }
        }
        if (!isIgnoringBatteryOptimizations) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Please disable battery optimization and enable AutoStart to ensure alarms ring reliably.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Disable Optimization")
                    }
                }
            }
        }

        if (!canDrawOverlays) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Please allow drawing over other apps to ensure alarms can wake the screen.",
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Allow Overlay")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !canUseFullScreenIntent) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Full-screen notifications must be enabled for alarms to wake your screen.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Enable Full-Screen Notifications")
                }
            }
        }
    }
}
