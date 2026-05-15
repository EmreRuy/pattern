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
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun settingsDao(): SettingsDao
}
