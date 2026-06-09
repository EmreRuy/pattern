package com.example.pattern.domain.repository

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.util.DataResult
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabitsStream(): Flow<DataResult<List<Habit>>>
    fun getHabitStream(id: Int): Flow<DataResult<Habit?>>
    fun getHabitWithHistoryStream(id: Int): Flow<DataResult<HabitWithHistory?>>
    suspend fun getHabitOnce(id: Int): Habit?
    suspend fun upsertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    
    fun getDailyStatesForDate(date: String): Flow<DataResult<List<HabitDailyState>>>
    fun getDailyStatesForHabit(habitId: Int): Flow<DataResult<List<HabitDailyState>>>
    fun getAllDailyStatesStream(): Flow<DataResult<List<HabitDailyState>>>
    fun getDailyStatesFromDateStream(startDate: String): Flow<DataResult<List<HabitDailyState>>>
    fun getCompletedDatesStream(): Flow<DataResult<Map<Int, Set<LocalDate>>>>
    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState>
    suspend fun upsertDailyState(state: HabitDailyState)
    suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState?
    suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean)
    suspend fun incrementTaskCount(habitId: Int, date: String)
    
    suspend fun <R> withTransaction(block: suspend () -> R): R
}
