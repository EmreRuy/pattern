package com.example.pattern.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pattern.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lead Expert Implementation:
 * NotificationHelper centralizes all notification-related logic.
 * It now supports multiple channels to distinguish between high-priority reminders
 * and general engagement notifications.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val HABIT_REMINDER_CHANNEL_ID = "habit_reminder_channel"
        const val HABIT_REMINDER_CHANNEL_NAME = "Habit Reminders"
        
        const val GENERAL_CHANNEL_ID = "general_channel"
        const val GENERAL_CHANNEL_NAME = "General Updates"
        
        const val GENERAL_NOTIFICATION_ID = 9999
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val habitChannel = NotificationChannel(
            HABIT_REMINDER_CHANNEL_ID,
            HABIT_REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for your habits"
            enableLights(true)
            enableVibration(true)
        }

        val generalChannel = NotificationChannel(
            GENERAL_CHANNEL_ID,
            GENERAL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Occasional tips and updates"
        }

        notificationManager.createNotificationChannels(listOf(habitChannel, generalChannel))
    }

    /**
     * Shows a high-priority exact reminder for a specific habit.
     */
    fun showHabitReminder(habitId: Int, habitName: String, motivation: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("HABIT_ID", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId, // Unique request code per habit
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HABIT_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for your habit!")
            .setContentText(motivation ?: "Stay consistent with: $habitName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(habitId, notification)
    }

    /**
     * Shows a general purpose notification for tips or streaks.
     */
    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            GENERAL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(GENERAL_NOTIFICATION_ID, notification)
    }
}
