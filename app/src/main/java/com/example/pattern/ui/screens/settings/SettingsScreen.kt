package com.example.pattern.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.R
import com.example.pattern.ui.components.DebouncedIconButton
import com.example.pattern.ui.components.PatternTimePickerDialog
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.theme.MossGreen
import com.example.pattern.utils.ReviewUtils
import com.example.pattern.utils.ShareUtils
import com.example.pattern.utils.SupportUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsContent(snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
fun SettingsContent(
    viewModel: SettingsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val backupViewModel: BackupViewModel = hiltViewModel()
    val backupUiState by backupViewModel.uiState.collectAsStateWithLifecycle()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { backupViewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirmation = true
        }
    }

    LaunchedEffect(backupUiState) {
        when (val state = backupUiState) {
            is BackupUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                backupViewModel.resetState()
            }
            is BackupUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                backupViewModel.resetState()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_section_preferences)) {
                SettingsNavigationItem(
                    Icons.Default.Palette,
                    stringResource(R.string.settings_item_theme)
                )
                SettingsNavigationItem(
                    Icons.Default.Language,
                    stringResource(R.string.settings_item_language)
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                SettingsSwitchItem(
                    icon = Icons.Default.Bedtime,
                    title = stringResource(R.string.settings_item_quiet_hours),
                    checked = settings.quietHoursEnabled,
                    iconTint = MossGreen,
                    onCheckedChange = { isEnabled ->
                        viewModel.updateQuietHours(
                            isEnabled,
                            settings.startTime,
                            settings.endTime
                        )
                    }
                )
                //alpha value to "gray out" the items when disabled
                val contentAlpha = if (settings.quietHoursEnabled) 1f else 0.38f

                Column(modifier = Modifier.alpha(contentAlpha)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    SettingsNavigationItem(
                        icon = Icons.Default.WbTwilight,
                        title = stringResource(R.string.settings_item_starts_at),
                        subtitle = settings.startTime,
                        // Only clickable if quietHoursEnabled is true
                        onClick = { if (settings.quietHoursEnabled) showStartTimePicker = true }
                    )
                    SettingsNavigationItem(
                        Icons.Default.WbSunny,
                        title = stringResource(R.string.settings_item_ends_at),
                        subtitle = settings.endTime,
                        // Only clickable if quietHoursEnabled is true
                        onClick = { if (settings.quietHoursEnabled) showEndTimePicker = true }
                    )
                }

                /*   SettingsSwitchItem(
                       icon = Icons.Default.Alarm,
                       title = "Smart reminders",
                       iconTint = MossGreen,
                       checked = true,
                       onCheckedChange = { /* ... */ }
                   ) */
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_backup)) {
                SettingsNavigationItem(
                    icon = Icons.Default.FileDownload,
                    title = stringResource(R.string.settings_item_export),
                    subtitle = stringResource(R.string.settings_item_export_desc),
                    onClick = { exportLauncher.launch("pattern_backup_${System.currentTimeMillis()}.json") }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                SettingsNavigationItem(
                    icon = Icons.Default.FileUpload,
                    title = stringResource(R.string.settings_item_import),
                    subtitle = stringResource(R.string.settings_item_import_desc),
                    onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) }
                )
                
                if (backupUiState is BackupUiState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        color = MossGreen
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                SettingsNavigationItem(
                    icon = Icons.Default.Share,
                    title = "Share with your friends ",
                    onClick = { ShareUtils.shareApp(context) }
                )
                SettingsNavigationItem(
                    Icons.Default.Star, stringResource(R.string.settings_rate_us),
                    onClick = { ReviewUtils.launchInAppReview(context, scope) }
                )
                SettingsNavigationItem(
                    Icons.Default.Email, stringResource(R.string.settings_item_contact_support),
                    onClick = { SupportUtils.sendSupportEmail(context) }
                )
            }
        }
    }
    // Helper function to satisfy the compiler
    val dismissAllPickers = {
        showStartTimePicker = false
        showEndTimePicker = false
    }
    //Time Picker Dialog
    if (showStartTimePicker) {
        PatternTimePickerDialog(
            initialTime = settings.startTime,
            onTimeSelected = { newTime ->
                dismissAllPickers()
                viewModel.updateQuietHours(true, newTime, settings.endTime)
            },
            onDismiss = dismissAllPickers
        )
    }

    if (showEndTimePicker) {
        PatternTimePickerDialog(
            initialTime = settings.endTime,
            onTimeSelected = { newTime ->
                dismissAllPickers()
                viewModel.updateQuietHours(true, settings.startTime, newTime)
            },
            onDismiss = dismissAllPickers
        )
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text(stringResource(R.string.settings_import_confirm_title)) },
            text = { Text(stringResource(R.string.settings_import_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let { backupViewModel.importBackup(it) }
                        showImportConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.settings_import_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
       SectionHeader(title, modifier =  Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            content()
        }
    }
}
@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MossGreen,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                letterSpacing = 0.5.sp
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    iconTint: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconTint,
                checkedBorderColor = iconTint,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
