package com.example.pattern.ui.screens.homeScreen.mapper

import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.streak.StreakCalculator
import com.example.pattern.domain.usecase.HabitProjectionData
import com.example.pattern.ui.mapper.toCardModel
import com.example.pattern.ui.model.HabitCardModel
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitProjectionMapper @Inject constructor() {
    fun map(data: HabitProjectionData, date: LocalDate, today: LocalDate = LocalDate.now()): List<HabitCardModel> {
        val dailyStatesForDate = data.dailyStates[date] ?: emptyMap()
        val isFuture = date.isAfter(today)

        return data.allHabits.mapNotNull { habit ->
            if (habit.isScheduledOn(date)) {
                val streak = if (!isFuture) {
                    data.streaks[habit.id]?.currentStreak ?: 0
                } else 0
                
                HabitWithStatus(
                    habit = habit,
                    dailyState = if (isFuture) null else dailyStatesForDate[habit.id],
                    currentStreak = streak,
                    isReadOnly = isFuture
                ).toCardModel()
            } else {
                null
            }
        }
    }
}
