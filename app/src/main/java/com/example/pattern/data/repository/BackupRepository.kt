package com.example.pattern.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.pattern.data.local.backup.AppBackupData
import com.example.pattern.data.local.db.AppDataBase
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

interface BackupRepository {
    suspend fun exportBackup(uri: Uri): Result<Unit>
    suspend fun importBackup(uri: Uri): Result<Unit>
}

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: AppDataBase,
    private val gson: Gson,
    @param:ApplicationContext private val context: Context
) : BackupRepository {

    override suspend fun exportBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val habitDao = database.habitDao()
            val settingsDao = database.settingsDao()

            val backupData = AppBackupData(
                habits = habitDao.getAllHabitsOnce(),
                habitDailyStates = habitDao.getAllDailyStatesOnce(),
                settings = settingsDao.getSettingsOnce()
            )

            val jsonString = gson.toJson(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: throw Exception("Could not open output stream for URI: $uri")
        }
    }

    override suspend fun importBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw Exception("Could not open input stream for URI: $uri")

            val backupData = gson.fromJson(jsonString, AppBackupData::class.java)
                ?: throw Exception("Failed to parse backup file")

            database.withTransaction {
                // Clear existing data
                database.clearAllTables()

                // Restore Habits
                val habitDao = database.habitDao()
                backupData.habits.forEach { habit ->
                    habitDao.upsertHabit(habit)
                }

                // Restore Daily States
                backupData.habitDailyStates.forEach { state ->
                    habitDao.upsertDailyState(state)
                }

                // Restore Settings
                backupData.settings?.let {
                    database.settingsDao().upsertSettings(it)
                }
            }
            Unit
        }
    }
}
