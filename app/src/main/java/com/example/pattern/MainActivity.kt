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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.navigation.NavActions
import com.example.pattern.ui.navigation.NavHost
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            // Keep the splash screen on-screen until we know where to navigate
            splashScreen.setKeepOnScreenCondition {
                isLoading || (startDestination == null)
            }

            AppTheme {
                if (startDestination != null) {
                    MainContent(startDestination!!)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(startDestination: String) {
        val viewModel: MainViewModel = hiltViewModel()
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Handle result */ }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        val navController = rememberNavController()
        val actions = remember(navController) { NavActions(navController) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screens.Home.route

        CompositionLocalProvider(LocalNavActions provides actions) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (Screens.shouldShowBottomBar(currentRoute)) {
                        CustomBottomBar(
                            currentRoute = currentRoute,
                            onItemClick = { item -> 
                                actions.navigateToBottomBarRoute(item.route) 
                            }
                        )
                    }
                },
                contentWindowInsets = WindowInsets.navigationBars
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues),
                    startDestination = startDestination,
                    onOnboardingFinish = {
                        viewModel.completeOnboarding()
                        actions.finishOnboarding()
                    }
                )
            }
        }
    }
}
