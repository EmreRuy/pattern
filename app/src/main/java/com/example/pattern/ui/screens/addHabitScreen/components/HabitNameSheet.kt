package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun HabitNameSheet(
    showSheet: Boolean,
    habitName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showSheet) {
        HabitNameInput(
            habitName = habitName,
            onNameChange = onNameChange,
            onDismiss = onDismiss,
        )
    }
}
