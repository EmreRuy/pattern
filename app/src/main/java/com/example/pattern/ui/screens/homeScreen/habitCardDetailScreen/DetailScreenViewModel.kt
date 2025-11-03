package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.Habit
import com.example.pattern.data.local.HabitRepository
import com.example.pattern.data.local.HabitType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class HabitDetailsViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])

    val habit: StateFlow<HabitDetailsUi?> =
        repository.getHabitStream(habitId)
            .map { habit -> habit?.toUi() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private fun Habit.toUi(): HabitDetailsUi {
        return HabitDetailsUi(
            id = id,
            name = name,
            icon = iconFromCode(iconCode),
            accentColor = accentFromIconCode(iconCode),
            currentStreak = if (isCompleted) 1 else 0, // placeholder for now
            totalCompletions = 0, // placeholder until you add a completion table
            goal = goalLabel(type, durationInMinutes),
            frequency = frequencyLabel(selectedDays),
            createdOn = createdAt.toUiDate()
        )
    }
}

fun iconFromCode(code: String): ImageVector = when (code) {
    "fitness" -> Icons.Default.FitnessCenter
    "book" -> Icons.Default.MenuBook
    "water" -> Icons.Default.WaterDrop
    "sleep" -> Icons.Default.Bedtime
    else -> Icons.Default.Star
}
fun accentFromIconCode(code: String) = when (code) {
    "fitness" -> Color(0xFF1E88E5) // Blue
    "book" -> Color(0xFF8E24AA) // Purple
    "water" -> Color(0xFF039BE5) // Light blue
    "sleep" -> Color(0xFF5E35B1) // Deep purple
    else -> Color(0xFF1E88E5)
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
fun Long.toUiDate(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

fun frequencyLabel(selectedDays: List<Boolean>): String {
    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    val active = days.zip(selectedDays).filter { it.second }.map { it.first }

    return when {
        active.size == 7 -> "Every day"
        active.size == 5 && active.containsAll(listOf("Mon","Tue","Wed","Thu","Fri")) -> "Weekdays"
        active.size == 2 && active.containsAll(listOf("Sat","Sun")) -> "Weekends"
        active.isEmpty() -> "No schedule"
        else -> active.joinToString(", ")
    }
}