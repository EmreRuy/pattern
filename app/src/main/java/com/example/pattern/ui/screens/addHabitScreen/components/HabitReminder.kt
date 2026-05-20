package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Optimization: Use a constant or remember the color to avoid allocation in composition
private val ReminderAccentColor = Color(0xFF6366F1)

@OptIn(ExperimentalMaterial3Api::class)
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
            modifier = Modifier.height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEnabled) {
                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.scale(0.85f),
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
}
