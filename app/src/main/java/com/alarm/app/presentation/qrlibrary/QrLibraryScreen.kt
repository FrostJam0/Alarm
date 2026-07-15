package com.alarm.app.presentation.qrlibrary

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.app.domain.model.QrCode

/**
 * The main screen for managing the user's library of QR codes.
 *
 * This screen displays a list of all registered QR codes (both scanned and generated).
 * Users can add new QR codes by either generating them or scanning existing ones.
 * Tapping on a QR code opens a detail sheet for further actions like renaming or deleting.
 *
 * @param viewModel The ViewModel handling the business logic and state for this screen.
 * @param onBack Callback invoked when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrLibraryScreen(
    viewModel: QrLibraryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showScanner by remember { mutableStateOf(false) }
    var selectedQrCode by remember { mutableStateOf<QrCodeItemUiState?>(null) }
    
    var showNameDialog by remember { mutableStateOf(false) }
    var pendingScannedValue by remember { mutableStateOf<String?>(null) }
    var newQrName by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("QR Code Library") })
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = { 
                    isGenerating = true
                    newQrName = ""
                    showNameDialog = true
                }) {
                    Text("Gen")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(onClick = { showScanner = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Scan to Register")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.qrCodes) { item ->
                QrLibraryCard(item = item, onClick = { selectedQrCode = item })
            }
            if (uiState.qrCodes.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        text = "No QR codes registered. Scan or generate one to begin.",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showScanner) {
        QrScannerSheet(
            onDismiss = { showScanner = false },
            onQrScanned = { value ->
                showScanner = false
                pendingScannedValue = value
                isGenerating = false
                newQrName = ""
                showNameDialog = true
            }
        )
    }

    selectedQrCode?.let { item ->
        QrDetailSheet(
            item = item,
            onDismiss = { selectedQrCode = null },
            onRename = { id, name -> viewModel.renameQrCode(id, name) },
            onDelete = { viewModel.deleteQrCode(item.qrCode) }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(if (isGenerating) "Name new QR Code" else "Name scanned QR Code") },
            text = {
                OutlinedTextField(
                    value = newQrName,
                    onValueChange = { newQrName = it },
                    label = { Text("Location (e.g. Bathroom)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newQrName.isNotBlank()) {
                        if (isGenerating) {
                            viewModel.generateAndRegisterQrCode(newQrName) { success, msg ->
                                if (!success) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            pendingScannedValue?.let { value ->
                                viewModel.registerScannedQrCode(newQrName, value) { success, msg ->
                                    if (!success) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showNameDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
