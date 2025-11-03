package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitDetailsUi(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val accentColor: Color,
    val currentStreak: Int,
    val totalCompletions: Int,
    val goal: String,
    val frequency: String,
    val createdOn: String
)