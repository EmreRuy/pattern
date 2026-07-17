package com.example.pattern

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.navigation.NavActions
import com.example.pattern.ui.navigation.NavHost
import com.example.pattern.ui.navigation.Screen
import com.example.pattern.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        handleIntent(intent)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = uiState) {
                        is MainUiState.Loading -> {
                            // Splash screen handles loading
                        }
                        is MainUiState.Success -> {
                            MainContent(
                                startDestination = state.startDestination,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        viewModel.handleIntent(intent)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(
        startDestination: Screen,
        viewModel: MainViewModel
    ) {
        val context = LocalContext.current
        val navController = rememberNavController()
        val actions = remember(navController) { NavActions(navController) }
        
        // Navigation Event Handler
        LaunchedEffect(viewModel.navigationEvents) {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateToDetail -> actions.navigateToDetail(event.habitId)
                }
            }
        }

        // Permission Logic
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Logic for permission result */ }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        val habitLimitStatus by viewModel.habitLimitStatus.collectAsStateWithLifecycle()

        CompositionLocalProvider(LocalNavActions provides actions) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                habitLimitStatus = habitLimitStatus ?: HabitLimitStatus.Loading,
                onOnboardingFinish = {
                    viewModel.completeOnboarding()
                    actions.finishOnboarding()
                }
            )
        }
    }
}
