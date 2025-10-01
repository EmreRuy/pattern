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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.addHabitScreen.AddHabitScreen
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pattern.ui.screens.profileScreen.ProfileScreen
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
                //for Bottom Sheet Control
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { it != SheetValue.PartiallyExpanded }
                )
                var showSheet by remember { mutableStateOf(false) }
                LaunchedEffect(showSheet) {
                    if (showSheet) {
                        sheetState.expand()
                    }
                }
                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState,
                    ) {
                        AddHabitScreen(onSaveSuccess = { showSheet = false } )
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        CustomBottomBar(
                            currentRoute = currentRoute,
                            onItemClick = { item ->
                                if (item.route == Screens.Add.route) {
                                    // Shows bottom sheet instead of navigating
                                    showSheet = true
                                } else if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screens.Home.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screens.Home.route) {
                            HomeScreen()
                        }
                        composable(Screens.Profile.route) {
                            ProfileScreen()
                        }
                    }
                }
            }
        }
    }
}