package com.example.pattern.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.scheduler.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lead Expert Implementation:
 * BootReceiver ensures all scheduled reminders are restored after a system reboot.
 * Alarms are not persistent across reboots, so we must manually reschedule them.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: HabitRepository

    @Inject
    lateinit var scheduler: ReminderScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val rescheduleActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            "android.intent.action.QUICKBOOT_POWERON"
        )

        if (action in rescheduleActions) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    // Fetch all habits and reschedule those that have reminders
                    val habits = repository.getAllHabitsStream().first()
                    habits.forEach { habit ->
                        if (habit.reminderTime != null) {
                            scheduler.schedule(habit)
                        }
                    }
                } catch (_: Exception) {
                    // Silently fail or log for system events
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
