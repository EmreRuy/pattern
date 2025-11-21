package com.example.pattern.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabit(id: Int): Flow<Habit?>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    //Daily State
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsertDailyState(state: HabitDailyState)

    @Query(
        """
        SELECT * FROM habit_daily_state
        WHERE habitId = :habitId AND date = :date
        LIMIT 1
        """
    )
    suspend fun getDailyStateOnce(
        habitId: Int,
        date: String
    ): HabitDailyState?

    @Query(
        """
        SELECT * FROM habit_daily_state
        WHERE date = :date
        """
    )
    fun getDailyStatesForDate(
        date: String
    ): Flow<List<HabitDailyState>>
}