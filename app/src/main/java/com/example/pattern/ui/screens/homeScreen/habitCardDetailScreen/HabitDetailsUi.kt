package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

@Immutable
data class HabitDetailsUi(
    val id: Int,
    val name: String,
    val icon: String?,
    val accentColor: Color,
    val currentStreak: Int,
    val totalCompletions: Int,
    val goal: String,
    val frequency: String,
    val createdOn: String,
    val createdAtLocalDate: LocalDate,
    val totalXP: Int,
    val motivation: String? = null,
    val completedDates: Set<String> = emptySet()
)
