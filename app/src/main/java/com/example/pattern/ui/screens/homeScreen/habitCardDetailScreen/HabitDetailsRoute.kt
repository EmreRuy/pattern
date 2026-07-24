package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Staff-level Route implementation.
 * Uses Crossfade to prevent "hard pops" between loading and content states.
 * For a premium experience, consider passing initial habit metadata (color, name)
 * to render a skeleton UI immediately.
 */
@Composable
fun HabitDetailsRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HabitDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Senior Developer Optimization: Sticky UI Pattern
    // We keep a reference to the last successful habit data.
    // When the habit is deleted, the ViewModel emits 'Error', but we 
    // keep showing the last known data while the screen is animating away.
    var lastSuccessHabit by remember { mutableStateOf<HabitDetailsUi?>(null) }
    
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is HabitDetailsUiState.Success) {
            lastSuccessHabit = state.habit
        }
    }

    Crossfade(
        targetState = uiState,
        animationSpec = tween(durationMillis = 300),
        label = "screen_transition"
    ) { state ->
        when (state) {
            HabitDetailsUiState.Loading -> {
                Box(Modifier.fillMaxSize())
            }
            HabitDetailsUiState.Error -> {
                // If we have sticky data, keep showing it to prevent the "white flash"
                if (lastSuccessHabit != null) {
                    HabitCardDetailsScreen(
                        habit = lastSuccessHabit!!,
                        onBack = onBack,
                        onDelete = {}, // Already deleting
                        onEdit = onEdit
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading habit")
                    }
                }
            }
            is HabitDetailsUiState.Success -> {
                HabitCardDetailsScreen(
                    habit = state.habit,
                    onBack = onBack,
                    onDelete = {
                        viewModel.deleteHabit(state.habit.id)
                        onBack()
                    },
                    onEdit = onEdit
                )
            }
        }
    }
}
