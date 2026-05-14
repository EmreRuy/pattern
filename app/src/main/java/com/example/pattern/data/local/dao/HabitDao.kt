package com.example.pattern.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitWithHistory
import com.example.pattern.data.local.entity.HabitCompletionDate
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Staff Engineer Refactoring:
 * Updated DAO queries to align with the snake_case schema naming conventions
 * and the new timer state machine fields.
 */
@Dao
interface HabitDao {

    @Upsert
    suspend fun upsertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Int)

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitWithHistory(id: Int): Flow<HabitWithHistory?>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabit(id: Int): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitOnce(id: Int): Habit?

    @Query("SELECT * FROM habits ORDER BY created_at DESC")
    fun getAllHabits(): Flow<List<Habit>>

    // Daily State

    @Upsert
    suspend fun upsertDailyState(state: HabitDailyState)

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE habit_id = :habitId AND date = :date
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
        WHERE habit_id = :habitId
        ORDER BY date DESC
    """)
    fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>>

    @Query("SELECT * FROM habit_daily_state")
    fun getAllDailyStates(): Flow<List<HabitDailyState>>

    @Query("""
        SELECT date FROM habit_daily_state
        WHERE habit_id = :habitId AND (is_completed = 1 OR is_task_completed = 1)
    """)
    fun getCompletedDatesForHabit(habitId: Int): Flow<List<String>>

    @Query("""
        SELECT habit_id AS habitId, date FROM habit_daily_state
        WHERE is_completed = 1 OR is_task_completed = 1
    """)
    fun getAllCompletedDates(): Flow<List<HabitCompletionDate>>

    @Query("SELECT * FROM habit_daily_state WHERE date >= :startDate")
    fun getDailyStatesFromDate(startDate: String): Flow<List<HabitDailyState>>

    @Query("""
        SELECT SUM(
            CASE 
                WHEN h.type = 'TASK' THEN 15
                WHEN h.type = 'QUIT' THEN 20
                WHEN h.type = 'BUILD' THEN 10 + (IFNULL(h.duration_in_minutes, 0) / 15) * 5
                ELSE 0
            END
        )
        FROM habit_daily_state ds
        JOIN habits h ON ds.habit_id = h.id
        WHERE ds.is_completed = 1 OR ds.is_task_completed = 1
    """)
    fun getTotalXP(): Flow<Int?>

    @Query("""
        SELECT * FROM habit_daily_state
        WHERE habit_id = :habitId
        ORDER BY date DESC
    """)
    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState>

    // Task Completion
    @Query("""
        UPDATE habit_daily_state
        SET is_task_completed = :completed
        WHERE habit_id = :habitId AND date = :date
    """)
    suspend fun setTaskCompleted(
        habitId: Int,
        date: String,
        completed: Boolean
    )
}
