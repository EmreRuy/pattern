package com.example.pattern.data.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime


@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Fetch settings from Room
        val settings = repository.getSettingsOnce() ?: SettingsEntity()

        if (settings.quietHoursEnabled) {
            val now = LocalTime.now()
            val start = LocalTime.parse(settings.startTime)
            val end = LocalTime.parse(settings.endTime)

            if (isInsideQuietHours(start, end, now)) {
                return Result.success()
            }
        }

        val habitName = inputData.getString("HABIT_NAME") ?: "Habit Reminder"
        showNotification(habitName)

        return Result.success()
    }

    private fun isInsideQuietHours(start: LocalTime, end: LocalTime, now: LocalTime): Boolean {
        return if (start.isBefore(end)) {
            !now.isBefore(start) && now.isBefore(end)
        } else {
            !now.isBefore(start) || now.isBefore(end)
        }
    }

    private fun showNotification(name: String) {
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)

        // Make sure "habit_channel" matches the ID in PatternApplication.kt
        val notification = NotificationCompat.Builder(applicationContext, "habit_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a system icon for now
            .setContentTitle("Pattern Reminder")
            .setContentText("Time to work on: $name")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Better for 2026 OS scheduling
            .setAutoCancel(true)
            .build()

        // Use a stable ID if you want habits to overwrite their own notifications,
        // or a random ID (like now) to stack them.
        notificationManager?.notify(name.hashCode(), notification)
    }
}