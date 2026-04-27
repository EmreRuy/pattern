package com.example.pattern.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "settings_table")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val quietHoursEnabled: Boolean = false,
    val startTime: String = "22:00",
    val endTime: String = "08:00",
    val totalXP: Int = 0
) {
    fun isQuietTime(now: LocalTime = LocalTime.now()): Boolean {
        if (!quietHoursEnabled) return false
        
        return try {
            val start = LocalTime.parse(startTime)
            val end = LocalTime.parse(endTime)
            
            if (start.isBefore(end)) {
                // Same day range (e.g., 14:00 - 16:00)
                !now.isBefore(start) && now.isBefore(end)
            } else {
                // Overnight range (e.g., 22:00 - 08:00)
                !now.isBefore(start) || now.isBefore(end)
            }
        } catch (_: Exception) {
            false
        }
    }
}