package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HabitDetailsRoute(
    onBack: () -> Unit,
    viewModel: HabitDetailsViewModel = hiltViewModel()
) {
    val habit = viewModel.habit.collectAsState().value
    var isDeleting by remember { mutableStateOf(false) }

    when {
        isDeleting -> {
            // Stay on the screen or show nothing while we navigate back
        }
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
                    isDeleting = true
                    viewModel.deleteHabit(habit.id)
                    onBack()
                }
            )
        }
    }
}