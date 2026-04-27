package com.example.pattern.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Upsert
    suspend fun upsertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabit(id: Int): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitOnce(id: Int): Habit?

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    // Daily State

    @Upsert
    suspend fun upsertDailyState(state: HabitDailyState)

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE habitId = :habitId AND date = :date
        LIMIT 1
    """)
    suspend fun getDailyStateOnce(
        habitId: Int,
        date: String
    ): HabitDailyState?

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE date = :date
    """)
    fun getDailyStatesForDate(
        date: String
    ): Flow<List<HabitDailyState>>

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE habitId = :habitId
        ORDER BY date DESC
    """)
    fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>>

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE habitId = :habitId
        ORDER BY date DESC
    """)
    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState>

    // Task Completion
    @Query("""
        UPDATE habit_daily_state
        SET isTaskCompleted = :completed
        WHERE habitId = :habitId AND date = :date
    """)
    suspend fun setTaskCompleted(
        habitId: Int,
        date: String,
        completed: Boolean
    )
}
