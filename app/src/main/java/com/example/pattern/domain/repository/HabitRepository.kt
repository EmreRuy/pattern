package com.example.pattern.domain.repository

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabitsStream(): Flow<List<Habit>>
    fun getHabitStream(id: Int): Flow<Habit?>
    fun getHabitWithHistoryStream(id: Int): Flow<HabitWithHistory?>
    suspend fun getHabitOnce(id: Int): Habit?
    suspend fun upsertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    
    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>>
    fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>>
    fun getAllDailyStatesStream(): Flow<List<HabitDailyState>>
    fun getDailyStatesFromDateStream(startDate: String): Flow<List<HabitDailyState>>
    fun getCompletedDatesStream(): Flow<Map<Int, Set<String>>>
    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState>
    suspend fun upsertDailyState(state: HabitDailyState)
    suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState?
    suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean)
    
    fun getTotalXPStream(): Flow<Int>
    fun getSettingsStream(): Flow<Settings?>
    suspend fun getSettingsOnce(): Settings?
    suspend fun updateQuietHours(enabled: Boolean, start: String, end: String)
    suspend fun addXP(xpToAdd: Int)
    
    suspend fun <R> withTransaction(block: suspend () -> R): R
}
