package com.example.pattern.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Habit::class],  // list of all tables
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
