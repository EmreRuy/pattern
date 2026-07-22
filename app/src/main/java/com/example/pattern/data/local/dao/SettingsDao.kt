package com.example.pattern.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pattern.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings_table LIMIT 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Query("SELECT * FROM settings_table LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Upsert // Modern Room alternative to @Insert(onConflict = REPLACE)
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("DELETE FROM settings_table")
    suspend fun deleteAllSettings()
}