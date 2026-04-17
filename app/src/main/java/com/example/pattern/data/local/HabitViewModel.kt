package com.example.pattern.data.local

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.data.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val habitList: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val application: Application
) : ViewModel() {
    /*
      The StateFlow that the HomeScreen will observe. It contains the list of all habits
     */
    val homeUiState: StateFlow<HomeUiState> = repository.getAllHabitsStream()
        .map { habits ->
            HomeUiState(habitList = habits)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> {
        return repository.getDailyStatesForDate(date)
    }

    fun saveNewHabit(
        name: String,
        type: HabitType,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<Boolean>,
        iconCode: String,
        accentColorHex: String
    ) {
        if (name.isBlank()) {
            println("Error: Habit name cannot be empty.")
            return
        }
        // Converts hours/minutes into a single Int in minutes
        val totalDurationInMinutes = if (type == HabitType.BUILD) {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        // Creates the Habit Entity object
        val newHabit = Habit(
            name = name.trim(),
            type = type,
            iconCode = iconCode,
            durationInMinutes = totalDurationInMinutes,
            selectedDays = selectedDays,
            accentColorHex = accentColorHex
        )
        viewModelScope.launch {
            try {
                repository.insertHabit(newHabit)
                scheduleHabitReminder(newHabit)
                println("Habit saved and reminder scheduled: ${newHabit.name}")

            } catch (e: Exception) {
                println("Failed to save habit: ${e.message}")
            }
        }
    }


    fun updateQuietHoursSettings(enabled: Boolean, start: String, end: String) {
        viewModelScope.launch {
            repository.updateQuietHours(enabled, start, end)
        }
    }
    val settingsState = repository.getSettingsStream()
        .map { it ?: SettingsEntity() } // If DB is empty, provide defaults
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    //Notifications quite hours // reminder
    fun scheduleHabitReminder(habit: Habit) {
        val workData = workDataOf("HABIT_NAME" to habit.name)

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(workData)
            .addTag("habit_${habit.id}")
            .build()

        // Access the context directly from the application object here
        WorkManager.getInstance(application.applicationContext).enqueueUniqueWork(
            "reminder_${habit.id}",
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            // Reschedule to update the notification data (e.g. new name)
            scheduleHabitReminder(habit)
        }
    }
    fun startTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date)
            if (currentDaily?.isCompleted == true) return@launch
            val updated = HabitDailyState(
                habitId = habitId,
                date = date,
                timerStartTime = System.currentTimeMillis(),
                timerPauseTime = null,
                isCompleted = false
            )
            repository.upsertDailyState(updated)
        }
    }

    fun pauseTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (currentDaily.isCompleted) return@launch
            if (currentDaily.timerStartTime == null) return@launch
            val updated = currentDaily.copy(
                timerPauseTime = System.currentTimeMillis()
            )
            repository.upsertDailyState(updated)
        }
    }

    fun resumeTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (currentDaily.isCompleted) return@launch
            if (currentDaily.timerStartTime == null) return@launch
            if (currentDaily.timerPauseTime == null) return@launch
            val now = System.currentTimeMillis()
            val pausedDuration = now - currentDaily.timerPauseTime
            val newStartTime = currentDaily.timerStartTime + pausedDuration
            val updated = currentDaily.copy(
                timerStartTime = newStartTime,
                timerPauseTime = null
            )
            repository.upsertDailyState(updated)
        }
    }

    fun finishTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date)
                ?: HabitDailyState(
                    habitId = habitId,
                    date = date
                )
            val updated = currentDaily.copy(
                isCompleted = true,
                timerStartTime = null,
                timerPauseTime = null
            )
            repository.upsertDailyState(updated)
        }
    }

    //For the task type of habit completion
    fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(habitId, date, completed)
        }
    }
    fun ensureDailyStateExists(habitId: Int, date: String) {
        viewModelScope.launch {
            val current = repository.getDailyStateOnce(habitId, date)
            if (current == null) {
                repository.upsertDailyState(
                    HabitDailyState(
                        habitId = habitId,
                        date = date,
                        isCompleted = false,
                        isTaskCompleted = false
                    )
                )
            }
        }
    }
}