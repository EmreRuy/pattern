package com.example.pattern.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.HabitDatabase
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.mapper.toDomain
import com.example.pattern.data.mapper.toLocal
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val database: HabitDatabase,
    private val habitDao: HabitDao,
    private val settingsDao: SettingsDao
) : HabitRepository {

    override fun getAllHabitsStream(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { list -> list.map { it.toDomain() } }
    }

    override fun getHabitStream(id: Int): Flow<Habit?> {
        return habitDao.getHabit(id).map { it?.toDomain() }
    }

    override fun getHabitWithHistoryStream(id: Int): Flow<com.example.pattern.domain.model.HabitWithHistory?> {
        return habitDao.getHabitWithHistory(id).map { it?.toDomain() }
    }

    override suspend fun getHabitOnce(id: Int): Habit? {
        return habitDao.getHabitOnce(id)?.toDomain()
    }

    override suspend fun upsertHabit(habit: Habit): Long {
        return habitDao.upsertHabit(habit.toLocal())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toLocal())
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.toLocal())
    }

    override fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForDate(date).map { list -> list.map { it.toDomain() } }

    override fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override fun getAllDailyStatesStream(): Flow<List<HabitDailyState>> =
        habitDao.getAllDailyStates().map { list -> list.map { it.toDomain() } }

    override fun getDailyStatesFromDateStream(startDate: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesFromDate(startDate).map { list -> list.map { it.toDomain() } }

    override suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState> =
        habitDao.getDailyStatesForHabitOnce(habitId).map { it.toDomain() }

    override suspend fun upsertDailyState(state: HabitDailyState) {
        try {
            habitDao.upsertDailyState(state.toLocal())
        } catch (e: Exception) {
            if (e is SQLiteConstraintException || e.message?.contains("FOREIGN KEY") == true) {
                Log.w("HabitRepository", "Foreign key violation for habit ${state.habitId}: likely deleted. Ignoring upsert.")
            } else {
                Log.e("HabitRepository", "Error upserting daily state for habit ${state.habitId}", e)
            }
        }
    }

    override suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState? =
        habitDao.getDailyStateOnce(habitId, date)?.toDomain()

    override suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        try {
            habitDao.setTaskCompleted(habitId, date, completed)
        } catch (e: Exception) {
            if (e is SQLiteConstraintException || e.message?.contains("FOREIGN KEY") == true) {
                Log.w("HabitRepository", "Foreign key violation in setTaskCompleted for habit $habitId: likely deleted.")
            } else {
                Log.e("HabitRepository", "Error in setTaskCompleted for habit $habitId", e)
            }
        }
    }

    override fun getTotalXPStream(): Flow<Int> = combine(
        habitDao.getAllHabits(),
        habitDao.getAllDailyStates()
    ) { habits, allStates ->
        val habitMap = habits.associateBy { it.id }
        allStates.sumOf { state ->
            val habit = habitMap[state.habitId]
            if (habit != null) {
                ExperienceUtils.calculateHabitXP(habit.toDomain(), state.toDomain())
            } else 0
        }
    }.flowOn(Dispatchers.Default)

    override fun getSettingsStream(): Flow<Settings?> = settingsDao.getSettingsFlow().map { it?.toDomain() }

    override suspend fun getSettingsOnce(): Settings? = settingsDao.getSettingsOnce()?.toDomain()

    override suspend fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
        settingsDao.upsertSettings(
            current.copy(
                quietHoursEnabled = enabled,
                startTime = start,
                endTime = end
            )
        )
    }

    override suspend fun addXP(xpToAdd: Int) {
        val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
        settingsDao.upsertSettings(
            current.copy(totalXP = current.totalXP + xpToAdd)
        )
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R {
        return database.withTransaction {
            block()
        }
    }
}
