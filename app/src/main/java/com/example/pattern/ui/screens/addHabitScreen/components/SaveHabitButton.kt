package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pattern.data.local.HabitType
import com.example.pattern.data.local.HabitViewModel
import java.time.DayOfWeek

@Composable
fun SaveHabitButton(
    habitName: String,
    habitType: String,
    buildHabitDays: List<DayOfWeek>,
    durationHours: Int,
    durationMinutes: Int,
    reminderEnabled: Boolean,
    emoji: String,
    habitViewModel: HabitViewModel,
    onSaveSuccess: () -> Unit
) {
    Box(modifier = Modifier.padding(bottom = 16.dp)) {
        Button(
            onClick = {
                val habitTypeEnum = when (habitType) {
                    "Build" -> HabitType.BUILD
                    "Quit" -> HabitType.QUIT
                    "Task" -> HabitType.TASK
                    else -> HabitType.BUILD
                }

                val dayListBooleans = DayOfWeek.entries.map { it in buildHabitDays }

                habitViewModel.saveNewHabit(
                    name = habitName,
                    type = habitTypeEnum,
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    selectedDays = dayListBooleans,
                    reminderEnabled = reminderEnabled,
                    iconCode = emoji
                )

                onSaveSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            enabled = habitName.isNotBlank() && emoji.isNotBlank()
        ) {
            Text("Save", style = MaterialTheme.typography.titleMedium)
        }
    }
}