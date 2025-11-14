package com.example.pattern.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Habit::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class) // Tell Room to use my custom Type Converters
abstract class HabitDatabase : RoomDatabase() {

    // Define the DAOs that belong to this database
    abstract fun habitDao(): HabitDao
}
