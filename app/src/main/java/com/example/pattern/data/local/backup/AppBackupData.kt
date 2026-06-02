package com.example.pattern.data.local.backup

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.SettingsEntity
import com.google.gson.annotations.SerializedName

/**
 * Principal-Level Data Model for full application backup.
 * This structure aggregates all Room entities into a single, versioned JSON schema.
 */
data class AppBackupData(
    @SerializedName("backup_version")
    val backupVersion: Int = 1,
    
    @SerializedName("habits")
    val habits: List<Habit>? = null,
    
    @SerializedName("habit_daily_states")
    val habitDailyStates: List<HabitDailyState>? = null,
    
    @SerializedName("settings")
    val settings: SettingsEntity? = null
)
