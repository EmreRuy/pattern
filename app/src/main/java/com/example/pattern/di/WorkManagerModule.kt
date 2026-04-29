package com.example.pattern.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return try {
            WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            // This case happens in Hilt tests where the Application class 
            // doesn't implement Configuration.Provider or isn't initialized.
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
            WorkManager.initialize(context, config)
            WorkManager.getInstance(context)
        }
    }
}
