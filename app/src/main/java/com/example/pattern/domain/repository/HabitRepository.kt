package com.example.pattern.domain.repository

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.model.Settings
import com.example.pattern.data.model.BackupDto
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabitsStream(): Flow<List<Habit>>
    fun getHabitStream(id: Int): Flow<Habit?>
    fun getHabitWithHistoryStream(id: Int): Flow<HabitWithHistory?>
    suspend fun getHabitOnce(id: Int): Habit?
    suspend fun upsertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    
    fun getSettingsStream(): Flow<Settings?>
    suspend fun getSettingsOnce(): Settings?
    suspend fun updateQuietHours(enabled: Boolean, start: String, end: String)
    
    suspend fun getBackupData(): BackupDto
    suspend fun restoreBackupData(backupDto: BackupDto)
    
    suspend fun <R> withTransaction(block: suspend () -> R): R
}
