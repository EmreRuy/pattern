package com.example.pattern.ui.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DebouncedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    debounceTime: Long = 1000L,
    content: @Composable () -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    val scope = rememberCoroutineScope()

    // Sync with external enabled state
    LaunchedEffect(enabled) {
        isEnabled = enabled
    }

    IconButton(
        onClick = {
            if (!isEnabled) return@IconButton

            isEnabled = false
            onClick()

            scope.launch {
                delay(debounceTime)
                isEnabled = true
            }
        },
        enabled = isEnabled,
        modifier = modifier,
        content = content
    )
}
