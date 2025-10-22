package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pattern.di.DaySelector
import com.example.pattern.ui.screens.profileScreen.components.WheelDurationPicker
import java.time.DayOfWeek


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowTypeOfHabit(
    selectedDays: List<DayOfWeek>,
    onDaysChange: (List<DayOfWeek>) -> Unit,
) {
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var showDurationPicker by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day Selector Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Select Habit Schedule",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DaySelector(
                selectedDays = selectedDays,
                onDaysChange = onDaysChange
            )
        }
        // Duration Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Set Daily Duration Goal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Clickable row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showDurationPicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTimeFilled,
                        contentDescription = "Duration Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Daily Goal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.6f
                        )
                    )
                }
                // Display Current Duration
                Text(
                    text = "${selectedHours}h ${selectedMinutes}m",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    // Bottom Sheet (remains unchanged as it was already clean and modern)
    if (showDurationPicker) {
        ModalBottomSheet(
            onDismissRequest = { showDurationPicker = false },
            sheetState = bottomSheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp
        ) {
            var tempHours by remember { mutableIntStateOf(selectedHours) }
            var tempMinutes by remember { mutableIntStateOf(selectedMinutes) }

            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                title = { Text("Select Duration") }
            )
            HorizontalDivider(
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            // this is gonna be changed to a new modern design on the new updates, now it has flaws
            WheelDurationPicker(
                durationHours = tempHours,
                durationMinutes = tempMinutes,
                onDurationChange = { hours, minutes ->
                    tempHours = hours
                    tempMinutes = minutes
                }
            )

            // Confirmation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { showDurationPicker = false },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = {
                        selectedHours = tempHours
                        selectedMinutes = tempMinutes
                        showDurationPicker = false
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirm", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}



