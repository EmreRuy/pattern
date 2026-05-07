package com.example.pattern.notifications

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.scheduler.ReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    private val scheduler: ReminderScheduler
) {

    /**
     * Schedules a reminder for the given habit using AlarmManager.
     * This uses setExactAndAllowWhileIdle for precision, which allows the alarm
     * to fire even when the device is in low-power idle (Doze) modes.
     * 
     * Battery Optimization Note:
     * While 'setExactAndAllowWhileIdle' is reliable, it is resource-intensive.
     * Android limits how often these can fire to preserve battery life.
     */
    fun scheduleReminder(habit: Habit) {
        if (habit.reminderTime == null) {
            cancelReminder(habit.id)
            return
        }
        scheduler.schedule(habit)
    }

    fun cancelReminder(habitId: Int) {
        // We need a Habit object to cancel, but we only have an ID.
        // We can create a dummy habit with just the ID since our scheduler 
        // uses the ID to identify the PendingIntent.
        val dummyHabit = Habit(
            id = habitId,
            name = "",
            type = com.example.pattern.domain.model.HabitType.BUILD,
            durationInMinutes = null,
            selectedDays = emptyList(),
            iconCode = "",
            isCompleted = false,
            createdAt = 0,
            accentColorHex = "",
            timerStartTime = null,
            timerPauseTime = null,
            reminderTime = null,
            motivation = null
        )
        scheduler.cancel(dummyHabit)
    }
}
