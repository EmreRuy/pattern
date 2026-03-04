package com.example.pattern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.addHabitScreen.AddHabitScreen
import com.example.pattern.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.ui.navigation.NavHost
import com.example.pattern.ui.screens.settings.SettingsBottomSheetContent
import com.example.pattern.ui.screens.homeScreen.components.HabitListBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screens.Home.route

                // Separate sheet states for each bottom sheet
                val addHabitSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { it != SheetValue.PartiallyExpanded }
                )
                val menuSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { it != SheetValue.PartiallyExpanded }
                )
                val settingsSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { it != SheetValue.PartiallyExpanded }
                )

                var showAddHabitSheet by remember { mutableStateOf(false) }
                var showMenuSheet by remember { mutableStateOf(false) }
                var showSettingsSheet by remember { mutableStateOf(false) }

                val habitViewModel: HabitViewModel = hiltViewModel()
                val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()

                // Expand sheets when triggered
                LaunchedEffect(showAddHabitSheet) {
                    if (showAddHabitSheet) addHabitSheetState.expand()
                }
                LaunchedEffect(showMenuSheet) {
                    if (showMenuSheet) menuSheetState.expand()
                }
                LaunchedEffect(showSettingsSheet) {
                    if (showSettingsSheet) settingsSheetState.expand()
                }

                // Add Habit Bottom Sheet
                if (showAddHabitSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showAddHabitSheet = false },
                        sheetState = addHabitSheetState
                    ) {
                        AddHabitScreen(onSaveSuccess = { showAddHabitSheet = false })
                    }
                }

                // Habit List Bottom Sheet
                if (showMenuSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showMenuSheet = false },
                        sheetState = menuSheetState
                    ) {
                        HabitListBottomSheet(
                            habits = uiState.habitList,
                            onHabitClick = { id ->
                                showMenuSheet = false
                                navController.navigate(Screens.HabitDetail.createRoute(id))
                            }
                        )
                    }
                }

                // Settings Bottom Sheet
                if (showSettingsSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSettingsSheet = false },
                        sheetState = settingsSheetState
                    ) {
                        SettingsBottomSheetContent(
                            onClose = { showSettingsSheet = false }
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        CustomBottomBar(
                            currentRoute = currentRoute,
                            onItemClick = { item ->
                                when (item.route) {
                                    Screens.Add.route -> showAddHabitSheet = true
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
                        showMenuSheet = { showMenuSheet = true },
                        showSettingsSheet = { showSettingsSheet = true },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}