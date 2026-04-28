package com.example.pattern.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: HabitType,
    val durationInMinutes: Int?,
    val selectedDays: List<Boolean>,
    val iconCode: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val accentColorHex: String = "#77DD77",
    val timerStartTime: Long? = null,
    val timerPauseTime: Long? = null,
    val reminderTime: String? = null, // Stores in "HH:mm" format
    val motivation: String? = null
)

enum class HabitType {
    BUILD,
    QUIT,
    TASK
}