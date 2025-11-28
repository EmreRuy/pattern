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
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.model.HabitCardModel

@Composable
fun HabitCards(
    habits: List<HabitCardModel>,
    paddingValues: PaddingValues,
    isToday: Boolean,
    onHabitChecked: () -> Unit,
    onTimerFinished: (HabitCardModel) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel) -> Unit,
    onPauseTimer: (HabitCardModel) -> Unit,
    onResumeTimer: (HabitCardModel) -> Unit,
) {
    val scrollUi = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollUi)
            .padding(paddingValues)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        habits.forEach { habit ->
            when (habit.type) {
                HabitType.BUILD -> HabitBuildCard(
                    habit = habit,
                    isToday = isToday,
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


