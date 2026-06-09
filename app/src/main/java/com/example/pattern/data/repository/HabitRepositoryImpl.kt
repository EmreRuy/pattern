package com.example.pattern.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.db.AppDataBase
import com.example.pattern.data.mapper.toDomain
import com.example.pattern.data.mapper.toLocal
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import com.example.pattern.domain.util.asResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val database: AppDataBase,
    private val habitDao: HabitDao
) : HabitRepository {

    override fun getAllHabitsStream(): Flow<DataResult<List<Habit>>> {
        return habitDao.getAllHabits()
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)
            .asResult()
    }

    override fun getHabitStream(id: Int): Flow<DataResult<Habit?>> {
        return habitDao.getHabit(id)
            .distinctUntilChanged()
            .map { it?.toDomain() }
            .flowOn(Dispatchers.Default)
            .asResult()
    }

    override fun getHabitWithHistoryStream(id: Int): Flow<DataResult<com.example.pattern.domain.model.HabitWithHistory?>> {
        return habitDao.getHabitWithHistory(id)
            .distinctUntilChanged()
            .map { it?.toDomain() }
            .flowOn(Dispatchers.Default)
            .asResult()
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

    override fun getDailyStatesForDate(date: String): Flow<DataResult<List<HabitDailyState>>> =
        habitDao.getDailyStatesForDate(date)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)
            .asResult()

    override fun getDailyStatesForHabit(habitId: Int): Flow<DataResult<List<HabitDailyState>>> =
        habitDao.getDailyStatesForHabit(habitId)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)
            .asResult()

    override fun getAllDailyStatesStream(): Flow<DataResult<List<HabitDailyState>>> =
        habitDao.getAllDailyStates()
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)
            .asResult()

    override fun getDailyStatesFromDateStream(startDate: String): Flow<DataResult<List<HabitDailyState>>> =
        habitDao.getDailyStatesFromDate(startDate)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)
            .asResult()

    override fun getCompletedDatesStream(): Flow<DataResult<Map<Int, Set<LocalDate>>>> {
        return habitDao.getAllCompletedDates()
            .distinctUntilChanged()
            .map { list ->
                list.groupBy({ it.habitId }, { LocalDate.parse(it.date) })
                    .mapValues { it.value.toSet() }
            }
            .flowOn(Dispatchers.Default)
            .asResult()
    }

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
            habitDao.safeSetTaskCompleted(habitId, date, completed)
        } catch (e: Exception) {
            if (e is SQLiteConstraintException || e.message?.contains("FOREIGN KEY") == true) {
                Log.w("HabitRepository", "Foreign key violation in setTaskCompleted for habit $habitId: likely deleted.")
            } else {
                Log.e("HabitRepository", "Error in setTaskCompleted for habit $habitId", e)
            }
        }
    }

    override suspend fun incrementTaskCount(habitId: Int, date: String) {
        try {
            habitDao.safeIncrementTaskCount(habitId, date)
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error in incrementTaskCount for habit $habitId", e)
        }
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R {
        return database.withTransaction {
            block()
        }
    }
}
