package com.alarm.app.presentation.qrlibrary

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alarm.app.core.util.QrCodeGenerator

/**
 * A bottom sheet dialog displaying the details of a specific QR code.
 *
 * This sheet allows the user to view the QR code image (if generated),
 * save it to the gallery, rename the QR code, or delete it from the library.
 *
 * @param item The UI state containing the QR code and its usage count.
 * @param onDismiss Callback invoked when the sheet should be dismissed.
 * @param onRename Callback invoked with the QR code ID and its new name.
 * @param onDelete Callback invoked with the QR code ID when it should be deleted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrDetailSheet(
    item: QrCodeItemUiState,
    onDismiss: () -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf(item.qrCode.name) }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (item.qrCode.isGenerated) {
                val bitmap = remember(item.qrCode.value) {
                    QrCodeGenerator.generate(item.qrCode.value)
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val saved = QrCodeGenerator.saveQrToGallery(context, it, name)
                        Toast.makeText(context, if(saved) "Saved to gallery" else "Failed to save", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Save to Gallery")
                    }
                }
            } else {
                Text("Scanned QR Code. No printable preview available.", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (item.alarmLabels.isNotEmpty()) {
                Text(
                    text = "Used by: ${item.alarmLabels.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            
            Button(
                onClick = {
                    onRename(item.qrCode.id, name)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Name")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (item.usageCount > 0) {
                        Toast.makeText(context, "${item.usageCount} alarm(s) will be orphaned.", Toast.LENGTH_LONG).show()
                    }
                    onDelete(item.qrCode.id)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
