package com.example.pattern.data.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitCard(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    var isChecked: MutableState<Boolean> = mutableStateOf(false),
    var isTimeChecked: MutableState<Boolean> = mutableStateOf(false)
)