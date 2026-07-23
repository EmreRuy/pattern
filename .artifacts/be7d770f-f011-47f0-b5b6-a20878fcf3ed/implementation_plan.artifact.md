# Streak Logic Refactoring & Optimization

Act as a Staff Android Engineer to audit and perfect the streak calculation logic. The current implementation has logic flaws regarding when a streak is considered "broken" and follows a utility-based approach that doesn't fully align with clean MVVM/Domain-driven design.

## User Review Required

> [!IMPORTANT]
> **Streak Definition Change**: I am proposing that a streak should NOT be considered broken just because it's not completed *yet* on a scheduled day. It only breaks if the day is finished (past midnight) or if a previous scheduled day was missed. This ensures the UI doesn't show "0" halfway through a day when the user still has time to complete their habit.

> [!NOTE]
> I will move the logic from a utility file to a dedicated `StreakCalculator` component within the domain layer to improve testability and architectural alignment.

## Proposed Changes

### Domain Layer

#### [NEW] [StreakCalculator.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/domain/streak/StreakCalculator.kt)
- Define a modern interface for streak calculations.
- Implementation will handle complex logic:
    - Current Streak (with today's grace period).
    - Longest Streak.
    - Total Completions.
    - Streak status (Active, At Risk, Broken).

#### [MODIFY] [StreakInfo.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/domain/model/StreakInfo.kt)
- Enhance the model to include `isAtRisk` or `isCompletedToday` to help the UI provide better feedback.

### Data/Utility Layer cleanup

#### [DELETE] [StreakUtils.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/utils/StreakUtils.kt)
- Remove the old utility file after migrating all usages.

### UI Layer / ViewModels

#### [MODIFY] [DetailScreenViewModel.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/habitCardDetailScreen/DetailScreenViewModel.kt)
- Inject the new `StreakCalculator` and use it for UI state mapping.

### Use Cases

#### [MODIFY] [GetHomeHabitsUseCase.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/app/src/main/java/com/example/pattern/domain/usecase/GetHomeHabitsUseCase.kt)
#### [MODIFY] [GetProfileStatsUseCase.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/app/src/main/java/com/example/pattern/domain/usecase/GetProfileStatsUseCase.kt)
- Update to use the new calculator.

## Verification Plan

### Automated Tests
- Create `StreakCalculatorTest.kt` with exhaustive scenarios:
    - **Everyday Habits**: Test gaps, consecutive days, today completion.
    - **Scheduled Days**: Test non-scheduled days (weekends) and how they preserve streaks.
    - **Edge Cases**: Creation date, empty history, multiple completions.
- Run tests using `./gradlew :app:testDebugUnitTest`.

### Manual Verification
- Deploy the app and verify the "Streak" badge on the home screen and detail screen.
- Verify that a streak doesn't drop to 0 in the middle of a scheduled day.
