package com.example.pattern.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: HabitType,
    val durationInMinutes: Int? = null, // Only for Build habits
    val selectedDays: List<Boolean>,
    val iconCode: String,
    val reminderEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HabitType {
    BUILD,
    QUIT,
    TASK
}