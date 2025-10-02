package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pattern.data.model.HabitCard

@Composable
fun HabitCards(
    habits: List<HabitCard>,
    paddingValues: PaddingValues,
    onHabitChecked: () -> Unit,
    onHabitTimeChecked: () -> Unit,
    onHabitCardClick: (Int) -> Unit,
) {
    val scrollUi = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollUi)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "My Habits",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            habits.forEach { habit ->
                HabitTaskCard(habit = habit, onHabitChecked = onHabitChecked, onHabitCardClick)
                HabitTimeCard(habit = habit, onHabitTimeChecked = onHabitTimeChecked)
            }
        }
    }

