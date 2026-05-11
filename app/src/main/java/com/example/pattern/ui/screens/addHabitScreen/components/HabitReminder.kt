package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Optimization: Use a constant or remember the color to avoid allocation in composition
private val ReminderAccentColor = Color(0xFF6366F1)

@Composable
fun HabitReminderCard(
    reminderTime: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onOpenTimePicker: () -> Unit
) {
    HabitSelectionCard(
        label = "Reminder",
        onClick = onOpenTimePicker
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEnabled) {
                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ReminderAccentColor,
                    uncheckedBorderColor = Color.Transparent,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}
