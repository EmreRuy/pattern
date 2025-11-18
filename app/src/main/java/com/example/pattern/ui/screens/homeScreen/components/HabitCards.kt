package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pattern.data.local.HabitType
import com.example.pattern.data.model.HabitCard

@Composable
fun HabitCards(
    habits: List<HabitCard>,
    paddingValues: PaddingValues,
    onHabitChecked: () -> Unit,
    onTimerFinished: (HabitCard) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCard) -> Unit,
    onPauseTimer: (HabitCard) -> Unit,
    onResumeTimer: (HabitCard) -> Unit,
) {
    val scrollUi = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollUi)
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        habits.forEach { habit ->
            when (habit.type) {
                HabitType.BUILD -> HabitBuildCard(
                    habit = habit,
                    onTimerFinished = onTimerFinished,
                    onCardClick = onHabitCardClick,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer
                )

                HabitType.QUIT, HabitType.TASK -> HabitTaskCard(
                    habit = habit,
                    onHabitChecked = onHabitChecked,
                    onCardClick = onHabitCardClick
                )
            }
        }
    }
}


