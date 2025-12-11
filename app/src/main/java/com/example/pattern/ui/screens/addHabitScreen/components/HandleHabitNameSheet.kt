package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun HandleHabitNameSheet(
    showSheet: Boolean,
    habitName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showSheet) {
        HabitNameBottomSheet(
            habitName = habitName,
            onNameChange = onNameChange,
            onDismiss = onDismiss
        )
    }
}
