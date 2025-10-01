package com.example.pattern.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val emoji: String,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val durationHours: Int?,
    val durationMinutes: Int?,
    val selectedDays: String?
)
