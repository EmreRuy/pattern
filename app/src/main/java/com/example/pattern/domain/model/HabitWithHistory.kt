package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

import kotlinx.collections.immutable.ImmutableList

@Immutable
data class HabitWithHistory(
    val habit: Habit,
    val history: ImmutableList<HabitDailyState>
)
