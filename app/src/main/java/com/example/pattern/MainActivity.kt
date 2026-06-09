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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Lead Expert Fix: Reactive Intent tracking to handle notifications flawlessly
    private val activityIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        activityIntent.value = intent
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val currentIntent by activityIntent

            // Staff-level fix: Reactive intent handling triggered on change
            LaunchedEffect(currentIntent) {
                viewModel.handleIntent(currentIntent)
            }

            // Keep the splash screen on-screen until state is fully resolved
            splashScreen.setKeepOnScreenCondition {
                uiState is MainUiState.Loading
            }

            AppTheme {
                // Lead Expert Fix: Solid Surface prevents white flashes during initialization/navigation
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = uiState) {
                        is MainUiState.Loading -> { /* Splash screen is visible */ }
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
        // Staff-level fix: Trigger reactive update for the intent
        activityIntent.value = intent
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(
        startDestination: String,
        viewModel: MainViewModel
    ) {
        val context = LocalContext.current
        val navController = rememberNavController()
        val actions = remember(navController) { NavActions(navController) }
        
        // Lead Expert Fix: Unified Navigation Event Observer
        LaunchedEffect(viewModel.navigationEvents) {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateToDetail -> actions.navigateToDetail(event.habitId)
                }
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Logic for permission result */ }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
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
