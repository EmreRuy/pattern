# Walkthrough - State Preservation Fix

I have fixed the issue where the calendar strip and habit pager would reset to "Today" when navigating back to the Home screen or after process death.

## Changes Made

### 1. ViewModel State Initialization
In [HomeViewModel.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/HomeViewModel.kt), I updated the `uiState` initialization to pull the `selectedDate` directly from `SavedStateHandle` for its `initialValue`.

> [!IMPORTANT]
> Previously, the `initialValue` was hardcoded to `LocalDate.now()`. This caused a "flash" of the current date which triggered the Pager to jump before the real preserved state could be emitted by the Flow.

### 2. Robust Side-Effect Synchronization
In [HomeScreen.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/HomeScreen.kt), I refactored the Pager synchronization logic:
- **Stale State Prevention**: Added `rememberUpdatedState` for the `onEvent` callback and `state.selectedDate`. This ensures that `LaunchedEffect` and `snapshotFlow` always use the latest values without needing to restart the entire effect (which can cause jitter).
- **Conflict Resolution**: Added a guard `!habitPagerState.isScrollInProgress` to the ViewModel-to-Pager synchronization. This prevents the ViewModel from forcing a scroll/jump while the user is actively swiping.
- **Improved Initial Pager State**: Since the ViewModel now provides the correct date from the first frame, `rememberPagerState`'s `initialPage` now correctly points to the preserved date immediately upon recreation.

## Verification Results

- **Navigation**: Navigating to detail screens and back now perfectly preserves the pager position and selected date.
- **Process Death**: Tested by initializing the ViewModel with a mock `SavedStateHandle`; the `initialValue` correctly picks up the saved date.
- **Visual Smoothness**: The synchronization between the habit pager (days) and the calendar selector (weeks) remains active during scrolling for immediate visual feedback.
