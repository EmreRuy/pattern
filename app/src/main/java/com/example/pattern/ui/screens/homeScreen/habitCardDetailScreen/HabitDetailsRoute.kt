package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HabitDetailsRoute(
    onBack: () -> Unit,
    viewModel: HabitDetailsViewModel = hiltViewModel()
) {
    val habit = viewModel.habit.collectAsState().value

    when {
        habit == null -> {
            // If loading, just show progress
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            HabitCardDetailsScreen(
                habit = habit,
                onBack = onBack,
                onDelete = {
                    viewModel.deleteHabit(habit.id)
                    onBack()
                }
            )
        }
    }
}