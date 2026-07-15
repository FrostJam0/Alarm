package com.alarm.app.presentation.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.app.domain.model.QrCode
import com.alarm.app.presentation.qrlibrary.QrLibraryViewModel

/**
 * A bottom sheet composable that allows the user to pick a QR code from the saved library.
 * Displays a list of available QR codes for assignment to an alarm.
 *
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 * @param onQrSelected Callback invoked when a QR code is selected, passing the selected [QrCode].
 * @param viewModel The view model supplying the list of saved QR codes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPickerSheet(
    onDismiss: () -> Unit,
    onQrSelected: (QrCode) -> Unit,
    viewModel: QrLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select QR Code", style = MaterialTheme.typography.titleLarge) },
        text = {
            if (uiState.qrCodes.isEmpty()) {
                Text(
                    text = "No QR codes found. Please go to the QR Library to register one.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(uiState.qrCodes) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQrSelected(item.qrCode) }
                                .padding(16.dp)
                        ) {
                            Text(item.qrCode.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
