package com.example.pattern.di

import android.content.Context
import androidx.room.Room
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            HabitDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideSettingsDao(database: HabitDatabase): SettingsDao {
        return database.settingsDao()
    }
}
