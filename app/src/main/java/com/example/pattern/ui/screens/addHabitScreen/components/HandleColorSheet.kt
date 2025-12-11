package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun HandleColorSheet(
    showColorSheet: Boolean,
    selectedColor: String,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showColorSheet) {
        ColorPickerBottomSheet(
            selectedColor = selectedColor,
            onColorSelected = {
                onColorChange(it)
                onDismiss()
            },
            onDismiss = onDismiss
        )
    }
}

