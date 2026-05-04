package com.example.pattern.notifications

import android.content.Context
import androidx.work.*
import com.example.pattern.domain.model.Habit
import com.example.pattern.data.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    private val workManager: WorkManager
) {

    fun scheduleReminder(habit: Habit) {
        val reminderTimeStr = habit.reminderTime ?: return
        
        val reminderTime = try {
            LocalTime.parse(reminderTimeStr)
        } catch (e: Exception) {
            return
        }

        val now = LocalDateTime.now()
        var scheduledDateTime = LocalDateTime.of(now.toLocalDate(), reminderTime)
        
        if (scheduledDateTime.isBefore(now)) {
            scheduledDateTime = scheduledDateTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduledDateTime).toMillis()

        val data = workDataOf(
            "HABIT_ID" to habit.id,
            "HABIT_NAME" to habit.name
        )

        // Periodic work for daily reminders
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .addTag("habit_reminder_${habit.id}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "reminder_${habit.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    fun cancelReminder(habitId: Int) {
        workManager.cancelUniqueWork("reminder_$habitId")
    }
}
