package com.example.pattern.data.repository

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.dao.HabitDao
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

// This one is for managing habit data
/*
It is responsible for:
 1. Exposing data to the rest of the app (ViewModel).
 2. Abstracting the data source (Room DAO).
 3. Handling data fetching logic
 */
@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {

    /**
    Retrieves all habits from the local database.
    The Flow stream ensures the HomeView automatically updates in real-time
     */
    fun getAllHabitsStream(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    /**
    Retrieves a single habit by ID.
     */
    fun getHabitStream(id: Int): Flow<Habit?> {
        return habitDao.getHabit(id)
    }

    /**
    This will be called when the user clicks 'Save' on the AddHabitScreen.
     */
    suspend fun insertHabit(habit: Habit) {
        habitDao.insert(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.delete(habit)
    }

    //For HabitDaily State, countdown timer problem fixed
    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForDate(date)

    suspend fun upsertDailyState(state: HabitDailyState) =
        habitDao.upsertDailyState(state)

    suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState? =
        habitDao.getDailyStateOnce(habitId, date)

    // For task type of habit completion
    suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        habitDao.setTaskCompleted(habitId, date, completed)
    }

}