package com.example.pattern

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.domain.model.ThemeConfig
import com.example.pattern.ui.navigation.Destination
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.navigation.NavActions
import com.example.pattern.ui.navigation.NavHost
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
            val isSplashReady by viewModel.isSplashReady.collectAsStateWithLifecycle()
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition {
                savedInstanceState == null && !isSplashReady
            }

            val darkTheme = when (themeConfig) {
                ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                ThemeConfig.LIGHT -> false
                ThemeConfig.DARK -> true
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose { }
            }

            AppTheme(
                darkTheme = darkTheme,
                dynamicColor = true
            ) {
                if (startDestination != null) {
                    MainContent(
                        viewModel = viewModel,
                        startDestination = startDestination!!,
                        onUiReady = viewModel::onUiReady
                    )
                }
            }
        }
    }
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(
        viewModel: MainViewModel,
        startDestination: Any,
        onUiReady: () -> Unit
    ) {
        val context = LocalContext.current
        val navController = rememberNavController()
        val actions = remember(navController) { NavActions(navController) }

        LaunchedEffect(navController) {
            val habitId = intent.getIntExtra("HABIT_ID", -1)
            if (habitId != -1) {
                intent.removeExtra("HABIT_ID")
                actions.navigateToDetail(habitId)
            }
        }

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

        CompositionLocalProvider(LocalNavActions provides actions) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    startDestination = startDestination,
                    onUiReady = onUiReady,
                    onOnboardingFinish = {
                        viewModel.completeOnboarding()
                        actions.finishOnboarding()
                    }
                )
            }
        }
    }
}
