package com.example.pattern

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pattern.notifications.DailyEngagementWorker
import com.example.pattern.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PatternApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Accessing notificationHelper ensures it's initialized and the channel is created.
        notificationHelper.javaClass
        
        scheduleDailyEngagementWork()
    }

    private fun scheduleDailyEngagementWork() {
        val workRequest = PeriodicWorkRequestBuilder<DailyEngagementWorker>(
            24, TimeUnit.HOURS // Repeat every 24 hours
        )
            .setInitialDelay(12, TimeUnit.HOURS) // Start after some delay
            .addTag("daily_engagement")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_engagement_work",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work to avoid resetting the timer
            workRequest
        )
    }
}
