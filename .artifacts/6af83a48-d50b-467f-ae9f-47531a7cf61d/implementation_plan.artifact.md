# Fix Timer State Persistence during Habit Edits

The user reported that if a "Grow" (timer-based) habit is running and then edited (specifically changing types back and forth in the UI), the timer appears "stuck" or preserved at the time when the edit started.

## Analysis

1.  **Timer Persistence**: The timer state (`activeSessionStartMs`) is stored in the `habit_daily_state` table.
2.  **Inconsistent Repository Logic**: `DailyLogRepositoryImpl.getDailyStatesForDate` currently ignores timer ticks in its `distinctUntilChanged` check, which can lead to stale UI states if other components rely on this stream.
3.  **Lack of Timer Handling in Edit**: The `EditHabitViewModel` does not account for a running timer when updating a habit. If a habit's structure or type changes while a timer is active, the database state can become inconsistent.
4.  **"Stuck" Timer**: If a timer is "stuck" at a value (like 00:44), it indicates that the `activeSessionStartMs` is null (paused) but `accumulatedTimeMs` contains the elapsed time. My investigation suggests that the current migration logic in `EditHabitViewModel` might be inadvertently leaving the timer in a "running" state for a non-timer habit, or failing to update correctly.

## Proposed Changes

### Domain & Repository Layer

#### [MODIFY] [DailyLogRepositoryImpl.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/data/repository/DailyLogRepositoryImpl.kt)
- Update `getDailyStatesForDate` to include `accumulatedTimeMs` and `activeSessionStartMs` in its `distinctUntilChanged` check to ensure all consumers see timer updates.

### UI / ViewModel Layer

#### [MODIFY] [EditHabitViewModel.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/addHabitScreen/EditHabitScreen.kt)
- **Automatic Pause on Update**: Modify `updateHabit` to check if a timer is currently running for the habit being edited.
- If running, calculate the elapsed time, add it to `accumulatedTimeMs`, and set `activeSessionStartMs` to `null` before saving. This ensures a clean state transition.
- **Improved Migration Logic**: Explicitly clear timer-related fields (`activeSessionStartMs`, `accumulatedTimeMs`) when transitioning a habit from "Grow" (Build) to another type, unless specifically mapping progress.

## Verification Plan

### Automated Tests
- Review existing unit tests for `UpdateHabitProgressUseCase` and `EditHabitViewModel`.

### Manual Verification
1.  **Timer Consistency**:
    - Start a timer habit on the Home screen.
    - Wait until it reaches a specific value (e.g., 00:44 remaining).
    - Open the Edit screen for this habit.
    - Change type to "Task" and then back to "Grow" (simulating the user's "give up" scenario).
    - Save the habit.
    - Verify that on the Home screen, the habit is correctly reflecting the progress. (It should either be paused at 00:44 or have continued counting down depending on whether we choose to pause on save).
    - **Expected Outcome**: The timer should not be "stuck" or reset to 0 unless intended.

2.  **Type Transition**:
    - Start a timer, then change habit to "Task" and save.
    - Verify the timer is removed and progress is either lost or migrated as per logic.
    - Change back to "Grow" and verify the timer starts from a fresh or correctly migrated state.
