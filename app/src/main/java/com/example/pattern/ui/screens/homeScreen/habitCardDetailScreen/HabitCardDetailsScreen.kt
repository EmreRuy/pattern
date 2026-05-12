package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R
import com.example.pattern.ui.components.*

import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCardDetailsScreen(
    habit: HabitDetailsUi,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val accentColor = habit.accentColor
    val scrollState = rememberScrollState()
    var showDeleteSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = habit.name.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    DebouncedIconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Habit",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Habit",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // 🔥 STREAK CARD
            StreakCard(
                currentStreak = habit.currentStreak,
                accentColor = accentColor
            )

            Spacer(Modifier.height(24.dp))

            // ⚡ PROGRESS / XP CARD
            HabitProgressCard(habit, accentColor)

            if (!habit.motivation.isNullOrBlank()) {
                Spacer(Modifier.height(24.dp))
                MotivationCard(motivation = habit.motivation)
            }

            Spacer(Modifier.height(24.dp))
            
            ActivityCard(
                completedDates = habit.completedDates.dates,
                accentColor = accentColor,
                createdAt = habit.createdAtLocalDate
            )

            Spacer(Modifier.height(24.dp))

            ManagementCard(habit = habit)

            Spacer(Modifier.height(48.dp))
        }

        if (showDeleteSheet) {
            DeleteBottomSheet(
                onDismiss = { showDeleteSheet = false },
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteSheet = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun MotivationCard(motivation: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(title = stringResource(R.string.detail_section_motivation))
            Spacer(Modifier.height(24.dp))
            Text(
                text = "“$motivation”",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    fontSize = when {
                        motivation.length > 150 -> 16.sp
                        motivation.length > 80 -> 18.sp
                        else -> 22.sp
                    }
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActivityCard(
    completedDates: Set<String>,
    accentColor: Color,
    createdAt: LocalDate
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(modifier = Modifier.padding(vertical = 28.dp)) {
            SectionHeader(
                title = stringResource(R.string.detail_section_activity),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            HabitHeatMap(
                completedDates = completedDates,
                accentColor = accentColor,
                createdAt = createdAt,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ManagementCard(habit: HabitDetailsUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(modifier = Modifier.padding(vertical = 28.dp)) {
            SectionHeader(
                title = stringResource(R.string.detail_section_management),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            DetailRow(
                Icons.Rounded.StarOutline,
                stringResource(R.string.detail_label_goal),
                habit.goal
            )
            DetailRow(
                Icons.Rounded.Repeat,
                stringResource(R.string.detail_label_frequency),
                habit.frequency
            )
            DetailRow(
                Icons.Rounded.Notifications,
                stringResource(R.string.detail_label_reminder),
                habit.reminderTime ?: stringResource(R.string.detail_no_reminder)
            )
            DetailRow(
                Icons.Rounded.CalendarToday,
                stringResource(R.string.detail_label_created),
                habit.createdOn,
                isLast = true
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        DeleteHabitConfirmation(
            onCancel = onDismiss,
            onConfirm = onConfirm
        )
    }
}
