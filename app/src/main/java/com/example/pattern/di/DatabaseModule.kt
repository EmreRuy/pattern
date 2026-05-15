package com.example.pattern.di

import android.content.Context
import androidx.room.Room
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDataBase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDataBase::class.java,
            "habit_database"
        )
            .fallbackToDestructiveMigration(false) // Safety: Never lose user data in prod
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDataBase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDao(db: AppDataBase): SettingsDao {
        return db.settingsDao()
    }
}
