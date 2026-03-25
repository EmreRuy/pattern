package com.example.pattern.ui.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.proLocked.LockedProWrapper
import com.example.pattern.ui.screens.proLocked.PatternProBanner
import com.example.pattern.ui.screens.profileScreen.components.ExperienceLevelCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileExtraCard
import com.example.pattern.ui.screens.profileScreen.components.ProfileStatCard

@Preview
@Composable
fun ProfileScreenPreview() {
    //ProfileScreen()
}

@Composable
fun ProfileScreen(
    isPro: Boolean = false, // This should come from  ViewModel/User State
    onOpenMenuSheet: () -> Unit,
    onOpenSettingsSheet: () -> Unit,
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopBar(
            onMenuClick = onOpenMenuSheet,
            onSettingsClick = onOpenSettingsSheet
        )
        if (!isPro) {
            PatternProBanner()
        }

        // FREE CARD , Accessible to everyone
        ExperienceLevelCard(
            title = "Progress Score",
            percentage = 0.75f,
            number = 100,
        )

        // LOCKED CARDS , Wrapped in the Pro logic
        LockedProWrapper(
            isLocked = !isPro,
        ) {
            ProfileExtraCard(
                title = "Your Streak",
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
                label = "Total Habits"
            )
        }

        LockedProWrapper(
            isLocked = !isPro,
        ) {
            ProfileStatCard(
                title = "Success Rate",
                percentage = 0.9f,
                number = 100,
                label = "Success Score"
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
