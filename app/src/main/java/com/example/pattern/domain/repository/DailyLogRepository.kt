package com.example.pattern.domain.repository

import com.example.pattern.domain.model.HabitDailyState
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DailyLogRepository {
    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>>
    fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>>
    fun getAllDailyStatesStream(): Flow<List<HabitDailyState>>
    fun getDailyStatesInRangeStream(startDate: String, endDate: String): Flow<List<HabitDailyState>>
    fun getDailyStatesFromDateStream(startDate: String): Flow<List<HabitDailyState>>
    fun getCompletedDatesStream(): Flow<Map<Int, Set<LocalDate>>>
    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState>
    suspend fun upsertDailyState(state: HabitDailyState)
    suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState?
    suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean)
    suspend fun incrementTaskCount(habitId: Int, date: String)
    
    fun getTotalXPStream(): Flow<Int>
    suspend fun addXP(xpToAdd: Int)
}
