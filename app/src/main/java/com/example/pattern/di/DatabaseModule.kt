package com.example.pattern.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pattern.data.local.dao.HabitDao
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideSettingsDao(db: HabitDatabase): SettingsDao {
        return db.settingsDao() // This must match the abstract fun in HabitDatabase.kt
    }
}

//Hilt is like super smart delivery service, instead of creating everything manually
// hilt create it once and deliver it everywhere when we need it
/* So we say:
“Hilt, please create one HabitDatabase when the app starts
and reuse that same instance everywhere (ViewModels, Repositories, etc.).” */
// This concept is called singleton, we don't create many databases for everything  but just once.

/*
The DatabaseModule is your app’s brain that teaches Hilt how to build
and share your Room database & DAO — so you never have to do it manually again.
 */