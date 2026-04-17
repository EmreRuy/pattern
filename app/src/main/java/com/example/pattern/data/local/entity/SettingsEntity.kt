package com.example.pattern.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0, // Always 0 to keep only one row
    val quietHoursEnabled: Boolean = false,
    val startTime: String = "22:00",
    val endTime: String = "07:00"
)