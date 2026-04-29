package com.example.pattern.ui.screens.homeScreen.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val todayStates: Map<Int, HabitDailyState> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<HabitListUiState> = combine(
        repository.getAllHabitsStream(),
        repository.getAllDailyStatesStream()
    ) { habits, allStates ->
        val today = LocalDate.now().toString()
        val todayStatesMap = allStates.filter { it.date == today }
            .associateBy { it.habitId }

        HabitListUiState(
            habits = habits,
            todayStates = todayStatesMap,
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
        .catch { e ->
        emit(HabitListUiState(error = e.message, isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState(isLoading = true)
    )
}
