package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun HabitTypeSheet(
    showSheet: Boolean,
    selectedType: String,
    selectedDays: List<DayOfWeek>,
    durationHours: Int,
    durationMinutes: Int,
    onTypeChange: (String) -> Unit,
    onDaysChange: (List<DayOfWeek>) -> Unit,
    onDurationChange: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (showSheet) {
        HabitTypeContent(
            selectedType = selectedType,
            selectedDays = selectedDays,
            durationHours = durationHours,
            durationMinutes = durationMinutes,
            onTypeChange = onTypeChange,
            onDaysChange = onDaysChange,
            onDurationChange = onDurationChange,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTypeContent(
    selectedType: String,
    selectedDays: List<DayOfWeek>,
    durationHours: Int,
    durationMinutes: Int,
    onTypeChange: (String) -> Unit,
    onDaysChange: (List<DayOfWeek>) -> Unit,
    onDurationChange: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(),
    ) {
        HabitTypeSelectorModern(
            selectedType = selectedType,
            onTypeChange = onTypeChange,
            selectedDays = selectedDays,
            onDaysChange = onDaysChange,
            durationHours = durationHours,
            durationMinutes = durationMinutes,
            onDurationChange = onDurationChange
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

