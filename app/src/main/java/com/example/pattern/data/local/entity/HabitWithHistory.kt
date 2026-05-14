package com.example.pattern.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Staff Engineer Refactoring:
 * Updated relation to use the corrected 'habit_id' column name.
 */
data class HabitWithHistory(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
    )
    val history: List<HabitDailyState>
)
