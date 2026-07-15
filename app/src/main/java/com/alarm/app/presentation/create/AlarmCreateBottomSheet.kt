package com.alarm.app.presentation.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.model.QrCode

/**
 * A bottom sheet composable for creating or editing an alarm.
 * Allows the user to set the alarm time, label, and select a QR code
 * required for dismissing the alarm.
 *
 * @param existingAlarm The alarm to edit, if any. Null for a new alarm.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 * @param viewModel The view model managing the creation and updating logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCreateBottomSheet(
    existingAlarm: Alarm?,
    onDismiss: () -> Unit,
    viewModel: AlarmCreateViewModel = hiltViewModel()
) {
    var hour by remember { mutableIntStateOf(existingAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(existingAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(existingAlarm?.label ?: "") }
    var repeatDays by remember { mutableIntStateOf(existingAlarm?.repeatDays ?: 0) }
    
    var selectedQrCode by remember { mutableStateOf<QrCode?>(null) }
    var existingQrId by remember { mutableIntStateOf(existingAlarm?.qrCodeId ?: -1) }
    var existingQrValue by remember { mutableStateOf(existingAlarm?.qrCodeValue ?: "") }

    var showQrPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    val context = LocalContext.current
    var ringtoneUri by remember { mutableStateOf(existingAlarm?.ringtoneUri) }
    var ringtoneTitle by remember { mutableStateOf("Default") }

    LaunchedEffect(ringtoneUri) {
        if (ringtoneUri != null) {
            try {
                val ringtone = RingtoneManager.getRingtone(context, Uri.parse(ringtoneUri))
                ringtoneTitle = ringtone.getTitle(context)
            } catch (e: Exception) { }
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            ringtoneUri = uri?.toString()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Set Alarm Time", style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.clickable { showTimePicker = true }
                )
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showQrPicker = true }
                    .padding(vertical = 16.dp)
            ) {
                Text("Dismiss with: ")
                Text(
                    text = selectedQrCode?.name ?: if (existingQrId != -1) "Assigned QR ($existingQrId)" else "None (Tap to select)",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            if (ringtoneUri != null) {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(ringtoneUri))
                            }
                        }
                        ringtonePickerLauncher.launch(intent)
                    }
                    .padding(vertical = 16.dp)
            ) {
                Text("Ringtone: ")
                Text(
                    text = ringtoneTitle,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val finalQrId = selectedQrCode?.id ?: existingQrId
                    val finalQrValue = selectedQrCode?.value ?: existingQrValue
                    if (finalQrId != -1) {
                        val alarm = Alarm(
                            id = existingAlarm?.id ?: 0,
                            hour = hour,
                            minute = minute,
                            label = label,
                            isEnabled = true,
                            repeatDays = repeatDays,
                            qrCodeId = finalQrId,
                            qrCodeValue = finalQrValue,
                            ringtoneUri = ringtoneUri,
                            createdAt = existingAlarm?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.saveAlarm(alarm)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedQrCode != null || existingQrId != -1
            ) {
                Text("Save Alarm")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showQrPicker) {
        QrPickerSheet(
            onDismiss = { showQrPicker = false },
            onQrSelected = { qr ->
                selectedQrCode = qr
                showQrPicker = false
            }
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
