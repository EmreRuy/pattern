package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class HabitWithHistory(
    val habit: Habit,
    val history: List<HabitDailyState>
)
