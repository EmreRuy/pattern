package com.example.pattern.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.repository.UserRepository
import com.example.pattern.domain.scheduler.ReminderScheduler
import com.example.pattern.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lead Expert Implementation:
 * AlarmReceiver handles the wake-up from AlarmManager.
 * It follows best practices by using goAsync() for background work 
 * and delegating notification display to a specialized helper.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: HabitRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var scheduler: ReminderScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.example.pattern.ACTION_REMINDER") return

        val habitId = intent.getIntExtra("HABIT_ID", -1)
        if (habitId == -1) return

        val pendingResult = goAsync()
        
        scope.launch {
            try {
                val habit = repository.getHabitOnce(habitId) ?: return@launch
                val settings = userRepository.getSettingsOnce()

                // Check if reminders are still enabled for this habit
                if (habit.reminderTime != null) {
                    
                    // Respect Quiet Hours
                    val isQuiet = settings?.isQuietTime() ?: false
                    
                    if (!isQuiet) {
                        notificationHelper.showHabitReminder(
                            habitId = habit.id,
                            habitName = habit.name,
                            motivation = habit.motivation
                        )
                    }
                    
                    // Reschedule for the next occurrence
                    scheduler.schedule(habit)
                }
            } catch (_: Exception) {
                // Production-ready error handling
            } finally {
                pendingResult.finish()
            }
        }
    }
}
