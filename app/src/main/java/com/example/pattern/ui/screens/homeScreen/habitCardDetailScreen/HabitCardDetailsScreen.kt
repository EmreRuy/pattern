package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.ui.res.stringResource
import com.example.pattern.R
import com.example.pattern.ui.components.HabitHeatMap
import com.example.pattern.ui.components.HabitProgressCard
import com.example.pattern.ui.screens.addHabitScreen.components.SectionHeader
import com.example.pattern.ui.screens.homeScreen.components.DebouncedIconButton


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
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                StreakCard(currentStreak = habit.currentStreak)

                Spacer(Modifier.height(24.dp))

                // ⚡ PROGRESS / XP CARD
                HabitProgressCard(habit, accentColor)

                Spacer(Modifier.height(32.dp))

                SectionHeader(stringResource(R.string.detail_section_activity))

                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                ) {
                    HabitHeatMap(
                        completedDates = habit.completedDates,
                        accentColor = accentColor,
                        createdAt = habit.createdAtLocalDate,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                if (!habit.motivation.isNullOrBlank()) {
                    Spacer(Modifier.height(32.dp))
                    SectionHeader(stringResource(R.string.detail_section_motivation))
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "“${habit.motivation}”",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 28.sp,
                                    textAlign = TextAlign.Center,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = when {
                                        habit.motivation.length > 150 -> 14.sp
                                        habit.motivation.length > 80 -> 16.sp
                                        else -> 20.sp
                                    }
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                SectionHeader(stringResource(R.string.detail_section_management))

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
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

                Spacer(Modifier.height(40.dp))
            }
        }

        if (showDeleteSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showDeleteSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                DeleteHabitConfirmation(
                    onCancel = { showDeleteSheet = false },
                    onConfirm = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteSheet = false
                        onDelete()
                    }
                )
            }
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
            .padding(horizontal = 20.dp, vertical = 14.dp),
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        )
    }
}