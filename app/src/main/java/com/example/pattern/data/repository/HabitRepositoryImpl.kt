package com.example.pattern.data.repository

import androidx.room.withTransaction
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.AppDataBase
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.model.BackupDto
import com.example.pattern.data.mapper.toDomain
import com.example.pattern.data.mapper.toLocal
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val database: AppDataBase,
    private val habitDao: HabitDao,
    private val settingsDao: SettingsDao
) : HabitRepository {

    override fun getAllHabitsStream(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
            .distinctUntilChanged { old, new ->
                if (old.size != new.size) return@distinctUntilChanged false
                old.zip(new).all { (o, n) ->
                    // Structural equality: ONLY items that change the UI definition
                    o.id == n.id &&
                    o.name == n.name &&
                    o.type == n.type &&
                    o.durationInMinutes == n.durationInMinutes &&
                    o.selectedDays == n.selectedDays &&
                    o.iconCode == n.iconCode &&
                    o.accentColorHex == n.accentColorHex &&
                    o.reminderTime == n.reminderTime
                }
            }
            .map { list -> list.map { it.toDomain() } }
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

    override fun getSettingsStream(): Flow<Settings?> = settingsDao.getSettingsFlow().map { it?.toDomain() }

    override suspend fun getSettingsOnce(): Settings? = settingsDao.getSettingsOnce()?.toDomain()

    override suspend fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        database.withTransaction {
            val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.upsertSettings(
                current.copy(
                    quietHoursEnabled = enabled,
                    startTime = start,
                    endTime = end
                )
            )
        }
    }

    override suspend fun getBackupData(): BackupDto {
        return BackupDto(
            habits = habitDao.getAllHabitsOnce(),
            dailyStates = habitDao.getAllDailyStates().first(),
            settings = settingsDao.getSettingsOnce()
        )
    }

    override suspend fun restoreBackupData(backupDto: BackupDto) {
        database.withTransaction {
            habitDao.deleteAllDailyStates()
            habitDao.deleteAllHabits()
            settingsDao.deleteAllSettings()

            backupDto.habits.forEach { habitDao.upsertHabit(it) }
            backupDto.dailyStates.forEach { habitDao.upsertDailyState(it) }
            backupDto.settings?.let { settingsDao.upsertSettings(it) }
        }
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R {
        return database.withTransaction {
            block()
        }
    }
}
