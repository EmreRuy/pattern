package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pattern.di.DaySelector
import java.time.DayOfWeek

@Composable
fun QuitTypeOfHabit(
    selectedDays: List<DayOfWeek>,
    onDaysChange: (List<DayOfWeek>) -> Unit
) {
    Column {
        Text(
            modifier = Modifier.padding(16.dp),
            text =  "Select the days you want to quit this habit:",
            style = MaterialTheme.typography.bodyMedium
        )
        DaySelector(
            selectedDays = selectedDays,
            onDaysChange = onDaysChange,
            modifier = Modifier.padding(12.dp)
        )
    }
}