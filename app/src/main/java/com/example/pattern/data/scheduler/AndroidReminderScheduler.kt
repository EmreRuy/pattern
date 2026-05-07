package com.example.pattern.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.pattern.data.receiver.AlarmReceiver
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.scheduler.ReminderScheduler
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(habit: Habit) {
        val reminderTime = habit.reminderTime ?: return
        val localTime = try {
            LocalTime.parse(reminderTime)
        } catch (e: Exception) {
            return
        }

        val nextTrigger = calculateNextTrigger(localTime, habit.selectedDays) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("HABIT_ID", habit.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setExactAndAllowWhileIdle ensures the alarm fires even in Doze mode.
        // Battery trade-off: Frequent exact alarms can drain battery, but for a habit tracker,
        // precision (reminding at the exact time) is often preferred by users.
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger.toInstant().toEpochMilli(),
                pendingIntent
            )
        } catch (e: SecurityException) {
            // On Android 12+, we might need SCHEDULE_EXACT_ALARM permission.
            // For simplicity in this implementation, we fallback to setAndAllowWhileIdle if it fails.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger.toInstant().toEpochMilli(),
                pendingIntent
            )
        }
    }

    override fun cancel(habit: Habit) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTrigger(time: LocalTime, selectedDays: List<Boolean>): ZonedDateTime? {
        if (selectedDays.isEmpty() || !selectedDays.contains(true)) return null

        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var triggerDateTime = now.with(time)

        val todayIndex = now.dayOfWeek.value - 1 // 1 (Mon) -> 0, ..., 7 (Sun) -> 6

        // Check if today is a selected day and the time hasn't passed yet
        if (selectedDays[todayIndex] && triggerDateTime.isAfter(now)) {
            return triggerDateTime
        }

        // Search for the next selected day in the coming week
        for (i in 1..7) {
            val nextDayIndex = (todayIndex + i) % 7
            if (selectedDays[nextDayIndex]) {
                return triggerDateTime.plusDays(i.toLong())
            }
        }

        return null
    }
}
