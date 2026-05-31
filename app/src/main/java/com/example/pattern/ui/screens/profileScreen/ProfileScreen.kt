package com.example.pattern.ui.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.profileScreen.components.*
import com.example.pattern.ui.screens.profileScreen.components.dashboard.ProfileExtraCard

/**
 * Staff-Engineered ProfileScreen.
 * 
 * Performance Fix:
 * Replaced the 'verticalScroll(Column)' with a 'LazyColumn'.
 * 
 * RATIONALE:
 * The previous implementation forced Jetpack Compose to measure and compose ALL heavy cards
 * (Charts, Pagers, Distributions) simultaneously on screen entry. This "Big Bang" inflation
 * choked the UI thread during navigation.
 * 
 * By using LazyColumn, we achieve "Visual Parity" while only composing cards as they 
 * enter the viewport, drastically reducing the initial frame budget.
 */
import com.example.pattern.ui.components.PremiumGuard

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onOpenMenuSheet: () -> Unit,
    onPremiumClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    PremiumGuard {
        ProfileScreenContent(
            viewModel = viewModel,
            onOpenMenuSheet = onOpenMenuSheet,
            onPremiumClick = onPremiumClick,
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
private fun ProfileScreenContent(
    viewModel: ProfileViewModel,
    onOpenMenuSheet: () -> Unit,
    onPremiumClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Top Bar - Static at the top of the list
        item(key = "top_bar") {
            HomeTopBar(
                onMenuClick = onOpenMenuSheet,
                onSettingsClick = onOpenSettings,
                onPremiumClick = onPremiumClick
            )
        }

        item(key = "spacer_top") {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. Behavioral Analysis Group (High Priority)
        item(key = "success_card") {
            ProfileExtraCard(uiState = uiState.successDashboard)
        }

        item(key = "streak_card") {
            StreakPerformanceCard(
                bestStreaks = uiState.bestStreaks,
                insight = uiState.streakInsight
            )
        }

        item(key = "active_days_card") {
            ActiveDaysAnalysisCard(analysis = uiState.activeDaysAnalysis)
        }
        
        item(key = "xp_dist_card") {
            XPDistributionCard(distribution = uiState.xpDistribution)
        }

        // 3. Historical Data Visualization (Heavy - Deferred via LazyColumn)
        item(key = "xp_chart_card") {
            XPProgressChartCard(
                title = "TOTAL XP GAINED",
                weeklyDataPoints = uiState.weeklyXpHistory,
                monthlyDataPoints = uiState.xpHistory,
                yearlyDataPoints = uiState.yearlyXpHistory
            )
        }
    }
}
