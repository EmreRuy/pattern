# Implementation Plan - Fix State Preservation for Calendar Strip

The user is experiencing an issue where the selected date and calendar pager position reset to a default value (e.g., Today) when navigating back to the main screen. This is caused by a mismatch between the ViewModel's initial state and the preserved state in `SavedStateHandle`, as well as a race condition/capture issue in the Compose `LaunchedEffect`.

## Proposed Changes

### [Home Screen & ViewModel](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen)

#### [MODIFY] [HomeViewModel.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/HomeViewModel.kt)
- Update the `uiState`'s `initialValue` in the `stateIn` operator to use `_selectedDate.value` instead of the default `LocalDate.now()`. This ensures that the very first emission of the `StateFlow` (even while loading) contains the correctly preserved date from `SavedStateHandle`.
- Ensure `_selectedDate` is initialized correctly from `SavedStateHandle`.

#### [MODIFY] [HomeScreen.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/HomeScreen.kt)
- Fix the `LaunchedEffect` that synchronizes the Pager state back to the ViewModel. It currently captures the initial `state` and doesn't update when `state.selectedDate` changes. I will use `rememberUpdatedState` to ensure the latest selected date is always used for comparison.
- Add a guard to the ViewModel -> Pager synchronization `LaunchedEffect` to avoid triggering scrolls while the user is actively swiping (`!habitPagerState.isScrollInProgress`).
- Ensure `PagerState` is initialized with the correct `initialPage` by leveraging the improved `uiState`.

## Verification Plan

### Manual Verification
1. Open the app.
2. Swipe the calendar strip to a future or past date (e.g., next Monday).
3. Navigate to a habit detail screen by clicking on a habit card.
4. Press the back button to return to the Home screen.
5. **Verify**: The calendar strip should still be on next Monday, and the selected date in the header should match.
6. Kill the app process (simulating process death) while on a specific date.
7. Re-open the app.
8. **Verify**: The app should restore to the previously selected date.
9. Test midnight transition: Change the device time to 23:59, open the app, wait for midnight.
10. **Verify**: The "Today" highlight should shift, and if "Today" was selected, the view should shift to the new "Today".
