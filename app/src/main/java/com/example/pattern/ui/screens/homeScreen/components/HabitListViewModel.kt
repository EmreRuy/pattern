package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.GetHabitListWithStatusUseCase
import com.example.pattern.domain.usecase.HabitStatusModel
import com.example.pattern.domain.usecase.HabitSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class HabitListUiState(
    val habits: ImmutableList<HabitStatusModel> = persistentListOf(),
    val summary: HabitSummary = HabitSummary(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Optimized ViewModel for the All Habits Screen.
 * 
 * Senior-Level Optimizations:
 * 1. Parallel State Calculation: Summary and Filtered list derived efficiently.
 * 2. Node-Depth Awareness: Data models designed for shallow UI tree rendering.
 * 3. Atomic Updates: State transitions are single-pass to avoid UI flickering.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getHabitListWithStatusUseCase: GetHabitListWithStatusUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val dateTicker = flow {
        while (true) {
            emit(LocalDate.now())
            delay(60_000)
        }
    }.distinctUntilChanged()

    private val debouncedSearchQuery = _searchQuery
        .debounce { query -> if (query.isEmpty()) 0L else 300L }
        .distinctUntilChanged()

    val uiState: StateFlow<HabitListUiState> = dateTicker
        .flatMapLatest { date -> getHabitListWithStatusUseCase(date) }
        .combine(debouncedSearchQuery) { habits, query ->
            // Calculate Global Summary (Instant & Constant)
            val total = habits.size
            val completed = habits.count { it.isCompleted }
            val dailyXP = habits.sumOf { it.currentXP }
            val summary = HabitSummary(total, completed, dailyXP)

            // Calculate Filtered & Sorted List (Alphabetical by default)
            val filtered = if (query.isBlank()) {
                habits
            } else {
                habits.filter { it.habit.name.contains(query, ignoreCase = true) }
            }
            
            val sorted = filtered.sortedBy { it.habit.name }

            HabitListUiState(
                habits = sorted.toImmutableList(),
                summary = summary,
                isLoading = false
            )
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .catch { e ->
            emit(HabitListUiState(error = e.message ?: "Unknown error", isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HabitListUiState()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
