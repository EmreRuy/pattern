package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.runtime.Composable

@Composable
fun HandleEmojiSheet(
    showEmojiSheet: Boolean,
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showEmojiSheet) {
        EmojiPickerBottomSheet(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = {
                onEmojiChange(it)
                onDismiss()
            },
            onDismiss = onDismiss
        )
    }
}