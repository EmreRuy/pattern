package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun EmojiPickerSheet(
    showEmojiSheet: Boolean,
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showEmojiSheet) {
        EmojiPickerContent(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = {
                onEmojiChange(it)
                onDismiss()
            },
            onDismiss = onDismiss
        )
    }
}