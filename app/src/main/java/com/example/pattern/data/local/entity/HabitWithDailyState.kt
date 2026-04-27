package com.example.pattern.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithDailyState(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val dailyStates: List<HabitDailyState>
)
