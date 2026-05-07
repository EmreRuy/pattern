package com.example.pattern.ui.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.R
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.profileScreen.components.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onOpenMenuSheet: () -> Unit,
    onPremiumClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val scroll = rememberScrollState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scroll)
    ) {
        HomeTopBar(
            onMenuClick = onOpenMenuSheet,
            onSettingsClick = onOpenSettings,
            onPremiumClick = onPremiumClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Behavioral Analysis Group
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Success Card - Premium Behavioral Insight
            ProfileExtraCard(uiState = uiState.successDashboard)

            StreakPerformanceCard(
                bestStreaks = uiState.bestStreaks,
                insight = uiState.streakInsight
            )

            ActiveDaysAnalysisCard(analysis = uiState.activeDaysAnalysis)
            
            XPDistributionCard(distribution = uiState.xpDistribution)
        }

        // 3. Historical Data Visualization
        XPProgressChartCard(
            title = "TOTAL XP GAINED",
            weeklyDataPoints = uiState.weeklyXpHistory,
            monthlyDataPoints = uiState.xpHistory,
            yearlyDataPoints = uiState.yearlyXpHistory
        )
        
        // Bottom padding for comfortable scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}
