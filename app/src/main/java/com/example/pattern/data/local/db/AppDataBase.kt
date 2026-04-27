package com.example.pattern.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pattern.data.local.converter.Converters
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.SettingsEntity

@Database(
    entities = [Habit::class, HabitDailyState::class, SettingsEntity::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class) // Tell Room to use my custom Type Converters
abstract class HabitDatabase : RoomDatabase() {

    // Define the DAOs that belong to this database
    abstract fun habitDao(): HabitDao
    abstract fun settingsDao(): SettingsDao
}
