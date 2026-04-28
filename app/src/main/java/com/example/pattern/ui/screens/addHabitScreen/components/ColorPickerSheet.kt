package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun ColorPickerSheet(
    showColorSheet: Boolean,
    selectedColor: String,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showColorSheet) {
        ColorPickerContent(
            selectedColor = selectedColor,
            onColorSelected = {
                onColorChange(it)
                onDismiss()
            },
            onDismiss = onDismiss
        )
    }
}

