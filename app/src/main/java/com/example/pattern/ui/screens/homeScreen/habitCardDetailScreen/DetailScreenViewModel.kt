package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen


import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.utils.calculateStreak
import com.example.pattern.utils.toUiDate
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.pattern.notifications.ReminderManager
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

@HiltViewModel
class HabitDetailsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])

    val habit: StateFlow<HabitDetailsUi?> =
        combine(
            repository.getHabitStream(habitId),
            repository.getDailyStatesForHabit(habitId)
        ) { habit, dailyStates ->
            habit?.toUi(dailyStates)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            val habitToDelete = repository.getHabitOnce(habitId)
            if (habitToDelete != null) {
                repository.deleteHabit(habitToDelete)
                reminderManager.cancelReminder(habitId)
            }
        }
    }

    private fun Habit.toUi(dailyStates: List<HabitDailyState>): HabitDetailsUi {
        val streakInfo = calculateStreak(this, dailyStates)
        return HabitDetailsUi(
            id = id,
            name = name,
            icon = iconCode,
            accentColor = Color(accentColorHex.toColorInt()),
            currentStreak = streakInfo.currentStreak,
            totalCompletions = streakInfo.totalCompletions,
            goal = goalLabel(type, durationInMinutes),
            frequency = frequencyLabel(selectedDays),
            createdOn = createdAt.toUiDate(),
            motivation = motivation
        )
    }
}

fun goalLabel(type: HabitType, minutes: Int?): String {
    if (type != HabitType.BUILD || minutes == null) return ""

    val h = minutes / 60
    val m = minutes % 60

    return when {
        h > 0 && m > 0 -> "$h h $m m"
        h > 0 -> "$h h"
        m > 0 -> "$m m"
        else -> ""
    }
}

fun frequencyLabel(selectedDays: List<Boolean>): String {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val active = days.zip(selectedDays).filter { it.second }.map { it.first }

    return when {
        active.size == 7 -> "Everyday"
        active.size == 5 && active.containsAll(
            listOf(
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri"
            )
        ) -> "Weekdays"

        active.size == 2 && active.containsAll(listOf("Sat", "Sun")) -> "Weekends"
        active.isEmpty() -> "No schedule"
        else -> active.joinToString(", ")
    }
}