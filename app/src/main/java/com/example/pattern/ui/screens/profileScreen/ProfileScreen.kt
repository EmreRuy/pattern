package com.example.pattern.ui.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.proLocked.LockedProWrapper
import com.example.pattern.ui.screens.proLocked.PatternProBanner
import com.example.pattern.ui.screens.profileScreen.components.ExperienceLevelCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileExtraCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileStatCard
import androidx.hilt.navigation.compose.hiltViewModel

@Preview
@Composable
fun ProfileScreenPreview() {
    //ProfileScreen()
}

@Composable
fun ProfileScreen(
    isPro: Boolean = false, // This should come from  ViewModel/User State
    viewModel: HabitViewModel = hiltViewModel(),
    onOpenMenuSheet: () -> Unit,
    onOpenSettingsSheet: () -> Unit,
    onPremiumClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val scroll = rememberScrollState()
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
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
        if (!isPro) {
            PatternProBanner()
        }

        // FREE CARD , Accessible to everyone
        ExperienceLevelCard(
            title = stringResource(R.string.profile_progress_score),
            level = levelInfo.level,
            levelTitle = levelInfo.title,
            progress = levelInfo.progress,
            totalXP = levelInfo.currentXP,
            nextLevelXP = levelInfo.nextLevelXP
        )

        // LOCKED CARDS , Wrapped in the Pro logic
        LockedProWrapper(
            isLocked = !isPro,
        ) {
            ProfileExtraCard(
                title = stringResource(R.string.profile_your_streak),
                percentage = 0.60f,
                number = 100
            )
        }

        LockedProWrapper(
            isLocked = !isPro,
        ) {
            ProfileStatCard(
                title = "Total Completed Tasks",
                percentage = 1f,
                number = 200,
                label = stringResource(R.string.profile_total_habits)
            )
        }

        LockedProWrapper(
            isLocked = !isPro,
        ) {
            ProfileStatCard(
                title = stringResource(R.string.profile_success_rate),
                percentage = 0.9f,
                number = 100,
                label = stringResource(R.string.profile_success_score)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
