package com.example.pattern.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton
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
    private val habitDao: HabitDao,
    private val settingsDao: SettingsDao
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

    suspend fun getHabitOnce(id: Int): Habit? {
        return habitDao.getHabitOnce(id)
    }

    /**
    This will be called when the user clicks 'Save' on the AddHabitScreen.
     */
    suspend fun upsertHabit(habit: Habit): Long {
        return habitDao.upsertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    //For HabitDaily State, countdown timer problem fixed
    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForDate(date)

    fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForHabit(habitId)

    suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState> =
        habitDao.getDailyStatesForHabitOnce(habitId)

    suspend fun upsertDailyState(state: HabitDailyState) {
        try {
            habitDao.upsertDailyState(state)
        } catch (_: SQLiteConstraintException) {
            Log.w("HabitRepository", "Foreign key violation: Habit ${state.habitId} likely deleted. Ignoring upsert.")
        }
    }

    suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState? =
        habitDao.getDailyStateOnce(habitId, date)

    // For task type of habit completion
    suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        habitDao.setTaskCompleted(habitId, date, completed)
    }

    //Settings (for Notifications)
    fun getSettingsStream() = settingsDao.getSettingsFlow()

    suspend fun getSettingsOnce() = settingsDao.getSettingsOnce()

    suspend fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        settingsDao.upsertSettings(
            SettingsEntity(
                quietHoursEnabled = enabled,
                startTime = start,
                endTime = end
            )
        )
    }

}