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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
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
import com.example.pattern.ui.screens.addHabitScreen.AddHabitContent
import com.example.pattern.ui.screens.homeScreen.components.HabitListContent
import com.example.pattern.ui.screens.settings.SettingsScreen
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
                val currentRoute = navBackStackEntry?.destination?.route ?: Screens.Home.route
                
                // Routes that should NOT show the bottom bar
                val hideBottomBarRoutes = listOf(
                    Screens.HabitDetail.route,
                    Screens.Add.route,
                    Screens.EditHabit.route,
                    Screens.Settings.route
                )
                val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes && 
                                          !currentRoute.startsWith("habit_detail_route/") &&
                                          !currentRoute.startsWith("edit_habit_route/") &&
                                          currentRoute != Screens.Add.route &&
                                          currentRoute != Screens.HabitList.route &&
                                          currentRoute != Screens.Settings.route

                val habitViewModel: HabitViewModel = hiltViewModel()
                val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()
                var activeSheet by remember { mutableStateOf<AppSheet>(AppSheet.None) }
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            CustomBottomBar(
                                currentRoute = currentRoute,
                                onItemClick = { item ->
                                    when (item.route) {
                                    Screens.Add.route -> {
                                        navController.navigate(Screens.Add.route)
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
                    },
                    contentWindowInsets = WindowInsets.navigationBars
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
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
                            AppSheet.AddHabit -> Unit // Moved to screen

                            AppSheet.Menu -> Unit // Moved to screen
                            AppSheet.Settings -> SettingsScreen(onBack = { activeSheet = AppSheet.None })
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