package com.example.pattern

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HabitE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stateSyncStressTest_AddHabit_VerifyImmediateHomeUpdate_And_AllHabitsSync() {
        val habitName = "Sync Stress Test Habit"

        // 1. Navigate to Add Habit Screen
        composeTestRule.onNodeWithContentDescription("Add").performClick()

        // 2. Fill Details
        composeTestRule.onNodeWithText("What's the goal?").performTextInput(habitName)
        
        // Select Color (Step navigation)
        composeTestRule.onNodeWithText("Color").performClick()
        composeTestRule.onNodeWithTag("color_#264653").performClick()

        // 3. The 'Immediate Back' Test: Click Save and verify immediate presence on Home Screen
        // This ensures the ViewModel's state update is fast enough.
        composeTestRule.onNodeWithContentDescription("Save").performClick()

        // Haptic/UI Check: Ensure we navigated back to Home Screen
        composeTestRule.onNodeWithContentDescription("Menu").assertIsDisplayed()

        // Verify immediate presence on Home Screen
        composeTestRule.onNodeWithText(habitName).assertIsDisplayed()

        // 4. Cross-Screen Verification: Check "All Habits" screen
        composeTestRule.onNodeWithContentDescription("Menu").performClick()

        // Verify it exists in All Habits list
        composeTestRule.onNodeWithText("ALL HABITS").assertIsDisplayed()
        composeTestRule.onNodeWithText(habitName).assertIsDisplayed()
        
        // Final sanity check: Back to home
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText(habitName).assertIsDisplayed()
    }
}
