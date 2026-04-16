package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek


data class HabitTypeData(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTypeSelectorModern(
    selectedType: String,
    onTypeChange: (String) -> Unit,
    selectedDays: List<DayOfWeek>,
    onDaysChange: (List<DayOfWeek>) -> Unit,
    durationHours: Int,
    durationMinutes: Int,
    onDurationChange: (Int, Int) -> Unit
) {
    val habitTypes = listOf(
        HabitTypeData("Grow", Icons.Default.AutoGraph, Color(0xFF22C55E)),
        HabitTypeData("Drop", Icons.Default.RemoveCircleOutline, Color(0xFFFB7185)),
        HabitTypeData("Task", Icons.Default.ChangeCircle, Color(0xFF6366F1))
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Focus".uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            habitTypes.forEach { typeData ->
                val isSelected = selectedType == typeData.label

                Surface(
                    onClick = { onTypeChange(typeData.label) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) typeData.color
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    color = if (isSelected) typeData.color.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = typeData.icon,
                            contentDescription = null,
                            tint = if (isSelected) typeData.color
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = typeData.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isSelected) typeData.color
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = selectedType,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            slideInVertically(
                                initialOffsetY = { it / 6 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ))
                        .togetherWith(fadeOut(animationSpec = tween(110)))
                },
                label = "PremiumHabitTransition"
            ) { targetType ->
                when (targetType) {
                "Grow" -> {
                    GrowTypeOfHabit(
                        selectedDays = selectedDays,
                        onDaysChange = onDaysChange,
                        durationHours = durationHours,
                        durationMinutes = durationMinutes,
                        onDurationChange = onDurationChange
                    )
                }

                "Drop" -> {
                    DropTypeOfHabit(
                        selectedDays = selectedDays,
                        onDaysChange = onDaysChange
                    )
                }

                "Task" -> {
                    TaskTypeOfHabits(
                        selectedDays = selectedDays,
                        onDaysChange = onDaysChange
                    )
                }
            }
        }
    }
    }
    }


