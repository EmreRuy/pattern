package com.example.pattern

import android.content.Context
import androidx.room.Room
import com.example.pattern.data.local.HabitDao
import com.example.pattern.data.local.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton instance of the Room database.
     * The @ApplicationContext dependency is provided automatically by Hilt.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            "habit_database"
        )
            // Since I use converters, allowing database migrations
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }
}