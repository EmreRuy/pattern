package com.example.pattern.data.local.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.pattern.data.local.converter.Converters
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.SettingsEntity


@Database(
    entities = [Habit::class, HabitDailyState::class, SettingsEntity::class],
    version = 5, // Incremented to remove redundant timer fields from Habit table
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDataBase.MigrationFrom1To2::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = AppDataBase.MigrationFrom4To5::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun settingsDao(): SettingsDao

    /**
     * Handles schema changes specifically moving from Version 1 to Version 2.
     * Put only the renames that happened in version 2 here.
     */
    @RenameColumn(tableName = "settings_table", fromColumnName = "quietHoursEnabled", toColumnName = "quiet_hours_enabled")
    @RenameColumn(tableName = "settings_table", fromColumnName = "startTime", toColumnName = "start_time")
    @RenameColumn(tableName = "settings_table", fromColumnName = "endTime", toColumnName = "end_time")
    @RenameColumn(tableName = "settings_table", fromColumnName = "totalXP", toColumnName = "total_xp")
    class MigrationFrom1To2 : AutoMigrationSpec

    /**
     * Handles schema changes specifically moving from Version 4 to Version 5.
     * Deletes redundant timer fields from the 'habits' table.
     */
    @DeleteColumn(tableName = "habits", columnName = "accumulated_time_ms")
    @DeleteColumn(tableName = "habits", columnName = "active_session_start_ms")
    class MigrationFrom4To5 : AutoMigrationSpec
}