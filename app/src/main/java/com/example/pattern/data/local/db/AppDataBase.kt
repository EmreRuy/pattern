package com.example.pattern.data.local.db

import androidx.room.AutoMigration
import androidx.room.Database
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
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDataBase.MyAutoMigration::class),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun settingsDao(): SettingsDao

    /**
     * We use RenameColumn to safely migrate the camelCase columns to snake_case 
     * without losing any user settings or XP data.
     */
    @RenameColumn(tableName = "settings_table", fromColumnName = "quietHoursEnabled", toColumnName = "quiet_hours_enabled")
    @RenameColumn(tableName = "settings_table", fromColumnName = "startTime", toColumnName = "start_time")
    @RenameColumn(tableName = "settings_table", fromColumnName = "endTime", toColumnName = "end_time")
    @RenameColumn(tableName = "settings_table", fromColumnName = "totalXP", toColumnName = "total_xp")
    class MyAutoMigration : AutoMigrationSpec
}
