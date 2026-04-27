package com.example.pattern

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.ui.navigation.NavHost
import com.example.pattern.ui.screens.addHabitScreen.AddHabitSheetContent
import com.example.pattern.ui.screens.homeScreen.components.HabitListSheetContent
import com.example.pattern.ui.screens.settings.SettingsSheetContent
import com.example.pattern.utils.PremiumPlanScreen
import dagger.hilt.android.AndroidEntryPoint

sealed class AppSheet {
    data object None : AppSheet()
    data object AddHabit : AppSheet()
    data object Menu : AppSheet()
    data object Settings : AppSheet()

    data object Premium: AppSheet()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            AppTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission result if needed
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute =
                    navBackStackEntry?.destination?.route ?: Screens.Home.route
                val habitViewModel: HabitViewModel = hiltViewModel()
                val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()
                var activeSheet by remember { mutableStateOf<AppSheet>(AppSheet.None) }
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        CustomBottomBar(
                            currentRoute = currentRoute,
                            onItemClick = { item ->
                                when (item.route) {
                                    Screens.Add.route -> {
                                        activeSheet = AppSheet.AddHabit
                                    }

                                    else -> {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        showMenuSheet = { activeSheet = AppSheet.Menu },
                        showSettingsSheet = { activeSheet = AppSheet.Settings },
                        onPremiumClick = {activeSheet = AppSheet.Premium}
                    )
                }
                if (activeSheet != AppSheet.None) {
                    ModalBottomSheet(
                        onDismissRequest = { activeSheet = AppSheet.None },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (activeSheet) {
                            AppSheet.AddHabit -> AddHabitSheetContent(
                                onClose = { activeSheet = AppSheet.None }
                            )

                            AppSheet.Menu -> HabitListSheetContent(
                                habits = uiState.habitList,
                                onHabitClick = { id ->
                                    activeSheet = AppSheet.None
                                    navController.navigate(
                                        Screens.HabitDetail.createRoute(id)
                                    )
                                }
                            )
                            AppSheet.Settings -> SettingsSheetContent()
                            AppSheet.Premium -> PremiumPlanScreen(
                                onPurchase = {
                                    // Handle the Billing Logic here later
                                    activeSheet = AppSheet.None
                                }
                            )
                            AppSheet.None -> Unit
                        }
                    }
                }
            }
        }
    }
}