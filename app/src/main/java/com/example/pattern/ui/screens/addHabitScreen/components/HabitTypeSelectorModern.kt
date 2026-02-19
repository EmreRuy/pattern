package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun HabitTypeSelectorModern(
    selectedType: String,
    onTypeChange: (String) -> Unit,
    selectedDays: List<DayOfWeek>,
    onDaysChange: (List<DayOfWeek>) -> Unit,
    durationHours: Int,
    durationMinutes: Int,
    onDurationChange: (Int, Int) -> Unit
) {
    val habitTypes = listOf(
        "Grow" to "🚀",
        "Drop" to "🛑",
        "Task" to "🔁"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Customize Your Habit",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            habitTypes.forEach { (type, emoji) ->
                val isSelected = selectedType == type

                Surface(
                    onClick = { onTypeChange(type) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(emoji)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(type)
                    }
                }
            }
        }

        when (selectedType) {
            "Grow" -> {
                GrowTypeOfHabit(
                    selectedDays = selectedDays,
                    onDaysChange = onDaysChange,
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    onDurationChange = onDurationChange
                )
            }

            "Drop" -> {
                DropTypeOfHabit(
                    selectedDays = selectedDays,
                    onDaysChange = onDaysChange
                )
            }

            "Task" -> {
                TaskTypeOfHabits(
                    selectedDays = selectedDays,
                    onDaysChange = onDaysChange
                )
            }
        }
    }
}
