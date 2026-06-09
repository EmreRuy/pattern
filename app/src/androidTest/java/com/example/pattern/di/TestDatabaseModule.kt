package com.example.pattern.di

import android.content.Context
import androidx.room.Room
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.AppDataBase
import com.google.gson.Gson
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
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideInMemoryDatabase(@ApplicationContext context: Context): AppDataBase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDataBase::class.java
        ).allowMainThreadQueries().build()
    }

    @Provides
    fun provideHabitDao(database: AppDataBase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideSettingsDao(database: AppDataBase): SettingsDao {
        return database.settingsDao()
    }
}
