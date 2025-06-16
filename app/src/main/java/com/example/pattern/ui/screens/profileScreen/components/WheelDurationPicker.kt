package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WheelDurationPicker(
    durationHours: Int,
    durationMinutes: Int,
    onDurationChange: (Int, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Displays selected duration
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${durationHours.toString().padStart(2, '0')}h " +
                        "${durationMinutes.toString().padStart(2, '0')}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    items = (0..23).map { it.toString().padStart(2, '0') },
                    selectedIndex = durationHours,
                    onSelectedIndexChange = { onDurationChange(it, durationMinutes) }
                )
                Text("Hours", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    items = (0..59 step 5).map { it.toString().padStart(2, '0') },
                    selectedIndex = durationMinutes / 5,
                    onSelectedIndexChange = { onDurationChange(durationHours, it * 5) }
                )
                Text("Minutes", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

