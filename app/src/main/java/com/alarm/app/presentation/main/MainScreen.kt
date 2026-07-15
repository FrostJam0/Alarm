package com.alarm.app.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.app.domain.model.Alarm
import com.alarm.app.presentation.create.AlarmCreateBottomSheet

/**
 * The main screen composable that displays a list of all alarms.
 * Provides a floating action button to create new alarms and handles navigation to other screens.
 *
 * @param viewModel The view model managing the list of alarms and their states.
 * @param onNavigateToQrLibrary Callback to navigate to the QR Code Library screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AlarmListViewModel = hiltViewModel(),
    onNavigateToQrLibrary: () -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
    var allPermissionsGranted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm") },
                actions = {
                    IconButton(onClick = onNavigateToQrLibrary) {
                        Icon(Icons.Default.List, contentDescription = "QR Library")
                    }
                }
            )
        },
        floatingActionButton = {
            if (allPermissionsGranted) {
                FloatingActionButton(onClick = { 
                    alarmToEdit = null
                    showCreateSheet = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PermissionsBanner(
                onAllPermissionsGranted = { allPermissionsGranted = true }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.deleteAlarm(alarm)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                    ) {
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                            onClick = {
                                alarmToEdit = alarm
                                showCreateSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        AlarmCreateBottomSheet(
            existingAlarm = alarmToEdit,
            onDismiss = { showCreateSheet = false }
        )
    }
}
