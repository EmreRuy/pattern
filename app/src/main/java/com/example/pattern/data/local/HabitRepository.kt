package com.example.pattern.data.local

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
     * Retrieves all habits from the local database.
     * The Flow stream ensures the HomeView automatically updates in real-time
     */
    fun getAllHabitsStream(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    /**
     * Retrieves a single habit by ID.
     */
    fun getHabitStream(id: Long): Flow<Habit?> {
        return habitDao.getHabit(id)
    }

    /**
     Inserts a new Habit into the database.
     This will be called when the user clicks 'Save' on the AddHabitScreen.
     */
    suspend fun insertHabit(habit: Habit) {
        habitDao.insert(habit)
    }

    /**
     Updates an existing Habit.
     */
    suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit)
    }

    /**
     * Deletes a Habit.
     */
    suspend fun deleteHabit(habit: Habit) {
        habitDao.delete(habit)
    }
}