package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.local.HabitViewModel
import java.time.DayOfWeek

@Composable
fun SaveHabitButton(
    habitName: String,
    habitType: String,
    buildHabitDays: List<DayOfWeek>,
    durationHours: Int,
    durationMinutes: Int,
    emoji: String,
    habitViewModel: HabitViewModel,
    onSaveSuccess: () -> Unit,
    accentColorHex: String,
    reminderEnabled: Boolean,
    reminderTime: String?,
    motivation: String,
    snackbarHostState: SnackbarHostState
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "buttonScale")
    val scope = rememberCoroutineScope()
    
    val isNameValid = habitName.isNotBlank()
    val isColorValid = accentColorHex.isNotBlank()
    val isDaysValid = buildHabitDays.isNotEmpty()
    
    val isValid = isNameValid && isColorValid && isDaysValid && emoji.isNotBlank()

    Button(
        onClick = {
            if (!isNameValid) {
                scope.launch { snackbarHostState.showSnackbar("Please set a habit name") }
                return@Button
            }
            if (!isDaysValid) {
                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                return@Button
            }
            if (!isColorValid) {
                scope.launch { snackbarHostState.showSnackbar("Please select a color") }
                return@Button
            }

            val habitTypeEnum = when (habitType) {
                "Grow" -> HabitType.BUILD
                "Drop" -> HabitType.QUIT
                "Task" -> HabitType.TASK
                else -> HabitType.BUILD
            }
            val dayListBooleans = DayOfWeek.entries.map { it in buildHabitDays }
            habitViewModel.saveNewHabit(
                name = habitName,
                type = habitTypeEnum,
                durationHours = if (habitTypeEnum == HabitType.BUILD) durationHours else 0,
                durationMinutes = if (habitTypeEnum == HabitType.BUILD) durationMinutes else 0,
                selectedDays = dayListBooleans,
                iconCode = emoji,
                accentColorHex = accentColorHex,
                reminderTime = if (reminderEnabled) reminderTime else null,
                motivation = motivation
            )
            onSaveSuccess()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Text(
            "CREATE PATTERN",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        )
    }
}
