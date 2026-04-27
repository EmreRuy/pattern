package com.example.pattern.data.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pattern.MainActivity
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime


@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getInt("HABIT_ID", -1)
        android.util.Log.d("ReminderWorker", "doWork started for habitId: $habitId")
        if (habitId == -1) {
            android.util.Log.e("ReminderWorker", "habitId is -1, failing")
            return Result.failure()
        }

        val habit = repository.getHabitOnce(habitId) ?: run {
            android.util.Log.e("ReminderWorker", "Habit not found in DB for id: $habitId")
            return Result.failure()
        }
        
        // Check if today is a selected day for this habit
        val today = LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val isSelectedDay = habit.selectedDays.getOrNull(today - 1) ?: false
        
        android.util.Log.d("ReminderWorker", "Habit: ${habit.name}, Today: $today, isSelectedDay: $isSelectedDay")

        if (!isSelectedDay) {
            android.util.Log.d("ReminderWorker", "Not a selected day for ${habit.name}, skipping")
            return Result.success() 
        }

        val settings = repository.getSettingsOnce() ?: SettingsEntity()
        if (settings.isQuietTime()) {
            android.util.Log.d("ReminderWorker", "Currently in quiet hours, skipping notification")
            return Result.success()
        }
        
        val habitName = inputData.getString("HABIT_NAME") ?: habit.name

        android.util.Log.d("ReminderWorker", "Showing notification for: $habitName")
        showNotification(habitName)

        return Result.success()
    }

    private fun showNotification(name: String) {
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        
        // Create Channel for Android 8.0+
        val channel = android.app.NotificationChannel(
            "habit_channel",
            "Habit Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for habit reminders"
        }
        notificationManager?.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("HABIT_NAME", name)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, "habit_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pattern Reminder")
            .setContentText("Time to work on: $name")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager?.notify(name.hashCode(), notification)
    }

    companion object {
        const val KEY_HABIT_ID = "HABIT_ID"
    }
}