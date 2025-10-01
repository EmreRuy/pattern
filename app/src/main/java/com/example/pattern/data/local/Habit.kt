package com.example.pattern.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: HabitType, // Stored as String (e.g., "Build", "Quit") and converted via Converters
    val durationInMinutes: Int? = null, // Only for Build habits
    val selectedDays: List<Boolean>, // Stored via TypeConverter: 7-item list (Mon to Sun)
    val iconCode: String, // The emoji string, e.g., "🔥"
    val reminderEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
enum class HabitType {
    BUILD,
    QUIT,
    TASK
}