package com.example.pattern.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.AppDataBase
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.mapper.toDomain
import com.example.pattern.data.mapper.toLocal
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.DailyLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLogRepositoryImpl @Inject constructor(
    private val database: AppDataBase,
    private val habitDao: HabitDao,
    private val settingsDao: SettingsDao
) : DailyLogRepository {

    override fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForDate(date)
            .distinctUntilChanged { old, new ->
                if (old.size != new.size) return@distinctUntilChanged false
                old.zip(new).all { (o, n) ->
                    // Structural equality for logging: ignore intermediate timer ticks
                    o.habitId == n.habitId && 
                    o.date == n.date && 
                    o.isCompleted == n.isCompleted && 
                    o.completedCount == n.completedCount &&
                    o.accumulatedTimeMs == n.accumulatedTimeMs &&
                    o.activeSessionStartMs == n.activeSessionStartMs
                }
            }
            .map { list -> list.map { it.toDomain() } }

    override fun getDailyStatesForHabit(habitId: Int): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override fun getAllDailyStatesStream(): Flow<List<HabitDailyState>> =
        habitDao.getAllDailyStates()
            .distinctUntilChanged { old, new ->
                if (old.size != new.size) return@distinctUntilChanged false
                old.zip(new).all { (o, n) ->
                    o.habitId == n.habitId && 
                    o.date == n.date && 
                    o.isCompleted == n.isCompleted && 
                    o.completedCount == n.completedCount &&
                    o.accumulatedTimeMs == n.accumulatedTimeMs &&
                    o.activeSessionStartMs == n.activeSessionStartMs
                }
            }
            .map { list -> list.map { it.toDomain() } }

    override fun getDailyStatesInRangeStream(startDate: String, endDate: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesInRange(startDate, endDate)
            .distinctUntilChanged { old, new ->
                if (old.size != new.size) return@distinctUntilChanged false
                old.zip(new).all { (o, n) ->
                    o.habitId == n.habitId && 
                    o.date == n.date && 
                    o.isCompleted == n.isCompleted && 
                    o.completedCount == n.completedCount &&
                    o.accumulatedTimeMs == n.accumulatedTimeMs &&
                    o.activeSessionStartMs == n.activeSessionStartMs
                }
            }
            .map { list -> list.map { it.toDomain() } }

    override fun getDailyStatesFromDateStream(startDate: String): Flow<List<HabitDailyState>> =
        habitDao.getDailyStatesFromDate(startDate).map { list -> list.map { it.toDomain() } }

    override fun getCompletedDatesStream(): Flow<Map<Int, Set<LocalDate>>> {
        return habitDao.getAllCompletedDates()
            .distinctUntilChanged()
            .map { list ->
                list.groupBy({ it.habitId }, { LocalDate.parse(it.date) })
                    .mapValues { it.value.toSet() }
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun getDailyStatesForHabitOnce(habitId: Int): List<HabitDailyState> =
        habitDao.getDailyStatesForHabitOnce(habitId).map { it.toDomain() }

    override suspend fun upsertDailyState(state: HabitDailyState) {
        try {
            habitDao.upsertDailyState(state.toLocal())
        } catch (e: Exception) {
            if (e is SQLiteConstraintException || e.message?.contains("FOREIGN KEY") == true) {
                Log.w("DailyLogRepository", "Foreign key violation for habit ${state.habitId}: likely deleted. Ignoring upsert.")
            } else {
                Log.e("DailyLogRepository", "Error upserting daily state for habit ${state.habitId}", e)
            }
        }
    }

    override suspend fun getDailyStateOnce(habitId: Int, date: String): HabitDailyState? =
        habitDao.getDailyStateOnce(habitId, date)?.toDomain()

    override suspend fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        try {
            database.withTransaction {
                val existing = habitDao.getDailyStateOnce(habitId, date)
                if (existing == null) {
                    habitDao.upsertDailyState(com.example.pattern.data.local.entity.HabitDailyState(habitId, date))
                }
                habitDao.setTaskCompleted(habitId, date, completed)
            }
        } catch (e: Exception) {
            if (e is SQLiteConstraintException || e.message?.contains("FOREIGN KEY") == true) {
                Log.w("DailyLogRepository", "Foreign key violation in setTaskCompleted for habit $habitId: likely deleted.")
            } else {
                Log.e("DailyLogRepository", "Error in setTaskCompleted for habit $habitId", e)
            }
        }
    }

    override suspend fun incrementTaskCount(habitId: Int, date: String) {
        try {
            database.withTransaction {
                val existing = habitDao.getDailyStateOnce(habitId, date)
                if (existing == null) {
                    habitDao.upsertDailyState(com.example.pattern.data.local.entity.HabitDailyState(habitId, date))
                }
                habitDao.incrementTaskCount(habitId, date)
            }
        } catch (e: Exception) {
            Log.e("DailyLogRepository", "Error in incrementTaskCount for habit $habitId", e)
        }
    }

    override fun getTotalXPStream(): Flow<Int> = 
        habitDao.getTotalXP().map { it ?: 0 }.flowOn(Dispatchers.Default)

    override suspend fun addXP(xpToAdd: Int) {
        database.withTransaction {
            val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.upsertSettings(
                current.copy(totalXP = current.totalXP + xpToAdd)
            )
        }
    }
}
