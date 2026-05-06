package com.example.pattern.ui.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.pattern.R
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar

import com.example.pattern.ui.screens.profileScreen.components.ExperienceLevelCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileExtraCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileStatCard
import com.example.pattern.ui.screens.profileScreen.components.XPDistributionCard
import com.example.pattern.ui.screens.profileScreen.components.XPProgressChartCard
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Preview
@Composable
fun ProfileScreenPreview() {
    //ProfileScreen()
}

@Composable
fun ProfileScreen(
    isPro: Boolean = false, // This should come from  ViewModel/User State
    viewModel: ProfileViewModel = hiltViewModel(),
    onOpenMenuSheet: () -> Unit,
    onPremiumClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val scroll = rememberScrollState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val levelInfo = uiState.levelInfo

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
      /*  if (!isPro) {
            PatternProBanner()
        } */

        // FREE CARD , Accessible to everyone
        levelInfo?.let {
            ExperienceLevelCard(
                title = stringResource(R.string.profile_progress_score),
                level = it.level,
                levelTitle = it.title,
                progress = it.progress,
                totalXP = it.currentXP,
                nextLevelXP = it.nextLevelXP
            )
        }

        XPProgressChartCard(
            title = "TOTAL XP GAINED",
            weeklyDataPoints = uiState.weeklyXpHistory,
            monthlyDataPoints = uiState.xpHistory,
            yearlyDataPoints = uiState.yearlyXpHistory
        )

        // LOCKED CARDS , Wrapped in the Pro logic

        // Success Score Card - Premium
        ProfileExtraCard(uiState = uiState.successDashboard)

        XPDistributionCard(distribution = uiState.xpDistribution)

        ProfileStatCard(
            title = "Overall Discipline",
            percentage = uiState.successRate,
            number = uiState.totalHabits,
            label = stringResource(R.string.profile_total_habits)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}
