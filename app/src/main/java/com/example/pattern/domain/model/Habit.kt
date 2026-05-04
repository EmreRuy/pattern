package com.example.pattern.domain.model

data class Habit(
    val id: Int,
    val name: String,
    val type: HabitType,
    val durationInMinutes: Int?,
    val selectedDays: List<Boolean>,
    val iconCode: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val accentColorHex: String,
    val timerStartTime: Long?,
    val timerPauseTime: Long?,
    val reminderTime: String?,
    val motivation: String?
)
