package com.example.pattern.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Staff Engineer Refactoring:
 * Refactored 'Habit' to use consistent timer state management.
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: HabitType,
    
    @ColumnInfo(name = "duration_in_minutes")
    val durationInMinutes: Int?,
    
    @ColumnInfo(name = "selected_days")
    val selectedDays: List<Boolean>,
    
    @ColumnInfo(name = "icon_code")
    val iconCode: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "accent_color_hex")
    val accentColorHex: String = "#77DD77",

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null, // Stores in "HH:mm" format
    
    @ColumnInfo(name = "motivation")
    val motivation: String? = null,

    @ColumnInfo(name = "task_count")
    val taskCount: Int? = null
)

enum class HabitType {
    BUILD,
    QUIT,
    TASK
}
