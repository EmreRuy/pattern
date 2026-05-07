package com.example.pattern.di

import android.content.Context
import com.example.pattern.data.scheduler.AndroidReminderScheduler
import com.example.pattern.domain.scheduler.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context
    ): ReminderScheduler {
        return AndroidReminderScheduler(context)
    }
}
