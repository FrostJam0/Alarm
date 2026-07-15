package com.alarm.app.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarm.app.domain.model.Alarm

/**
 * A composable card displaying the details of an alarm.
 * Shows the time, label, QR assignment status, and a switch to toggle the alarm on/off.
 *
 * @param alarm The alarm object containing the details to display.
 * @param onToggle Callback invoked when the enable/disable switch is toggled.
 * @param onClick Callback invoked when the card is clicked (e.g., to edit the alarm).
 * @param modifier The modifier to be applied to the card.
 */
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    fontSize = 32.sp,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Text(
                    text = alarm.label.ifEmpty { "Alarm" },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (alarm.qrCodeId == -1) {
                    Text(
                        text = "No QR code assigned",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                var timeRemainingText by remember { mutableStateOf("") }
                LaunchedEffect(alarm.hour, alarm.minute, alarm.isEnabled) {
                    if (!alarm.isEnabled) {
                        timeRemainingText = "Off"
                        return@LaunchedEffect
                    }
                    while(true) {
                        val now = Calendar.getInstance()
                        var diffHour = alarm.hour - now.get(Calendar.HOUR_OF_DAY)
                        var diffMin = alarm.minute - now.get(Calendar.MINUTE)
                        
                        if (diffMin < 0) {
                            diffMin += 60
                            diffHour -= 1
                        }
                        if (diffHour < 0) {
                            diffHour += 24
                        }
                        
                        if (diffHour == 0 && diffMin == 0) {
                             timeRemainingText = "Ringing now or in 24 hours"
                        } else {
                             val hStr = if (diffHour > 0) "$diffHour hour${if (diffHour > 1) "s" else ""}" else ""
                             val mStr = if (diffMin > 0) "$diffMin minute${if (diffMin > 1) "s" else ""}" else ""
                             val joiner = if (hStr.isNotEmpty() && mStr.isNotEmpty()) " and " else ""
                             timeRemainingText = "Rings in $hStr$joiner$mStr"
                        }
                        
                        val secondsToNextMinute = 60 - now.get(Calendar.SECOND)
                        delay(secondsToNextMinute * 1000L)
                    }
                }
                
                Text(
                    text = timeRemainingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                enabled = alarm.qrCodeId != -1 // Can't enable without QR
            )
        }
    }
}
