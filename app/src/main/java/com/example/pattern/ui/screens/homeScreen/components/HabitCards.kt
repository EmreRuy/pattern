package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onTaskCompleted: (Int, Boolean) -> Unit,
    onTimerFinished: (HabitCardModel) -> Unit,
    onUnfinishTimer: (Int) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel) -> Unit,
    onPauseTimer: (HabitCardModel) -> Unit,
    onResumeTimer: (HabitCardModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(
            items = habits,
            key = { it.id },
            contentType = { it.type }
        ) { habit ->
            when (habit.type) {
                HabitType.BUILD -> HabitBuildCard(
                    habit = habit,
                    isToday = isToday,
                    onTimerFinished = onTimerFinished,
                    onUnfinishTimer = onUnfinishTimer,
                    onCardClick = onHabitCardClick,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer
                )

                HabitType.TASK -> HabitTaskCard(
                    habit = habit,
                    isToday = isToday,
                    onTaskCompleted = onTaskCompleted,
                    onCardClick = onHabitCardClick
                )

                HabitType.QUIT -> HabitQuitCard(
                    habit = habit,
                    isToday = isToday,
                    onTaskCompleted = onTaskCompleted,
                    onCardClick = onHabitCardClick
                )
            }
        }
    }
}


