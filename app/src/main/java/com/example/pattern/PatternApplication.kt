package com.example.pattern

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PatternApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Habit Reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        // Note: The AlarmReceiver also ensures this channel exists, 
        // but creating it at App startup is good practice.
        val channel = NotificationChannel("habit_reminders", name, importance).apply {
            description = "Notifications for your daily habits and goals"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}
