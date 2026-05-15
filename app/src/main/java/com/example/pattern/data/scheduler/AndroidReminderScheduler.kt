package com.example.pattern.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.pattern.data.receiver.AlarmReceiver
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.scheduler.ReminderScheduler
import com.example.pattern.utils.ReminderUtils
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun schedule(habit: Habit) {
        val reminderTimeStr = habit.reminderTime ?: return
        
        // 1. Parse Time
        val reminderTime = try {
            LocalTime.parse(reminderTimeStr, timeFormatter)
        } catch (e: Exception) {
            return
        }

        // 2. Calculate Next Trigger
        val nextTrigger = ReminderUtils.calculateNextTrigger(
            reminderTime, 
            habit.selectedDays,
            ZonedDateTime.now(ZoneId.systemDefault())
        ) ?: return
        val triggerAtMillis = nextTrigger.toInstant().toEpochMilli()

        // 3. Create Intent
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("HABIT_ID", habit.id)
            action = "com.example.pattern.ACTION_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Check Permission for Exact Alarm (Android 12+)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        // 5. Schedule
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(habit: Habit) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.pattern.ACTION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
