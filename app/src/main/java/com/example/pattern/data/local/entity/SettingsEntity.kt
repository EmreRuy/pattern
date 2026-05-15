package com.example.pattern.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Staff Engineer Refactoring:
 * 1. Schema: Implemented explicit @ColumnInfo with snake_case naming.
 *    This ensures that renaming Kotlin properties won't break the database schema
 *    and follows SQL naming conventions.
 * 2. Precision: Encapsulated quiet time logic within the entity to keep it as
 *    the single source of truth for business rules related to settings.
 */
@Entity(tableName = "settings_table")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "quiet_hours_enabled")
    val quietHoursEnabled: Boolean = false,

    @ColumnInfo(name = "start_time")
    val startTime: String = "22:00",

    @ColumnInfo(name = "end_time")
    val endTime: String = "08:00",

    @ColumnInfo(name = "total_xp")
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