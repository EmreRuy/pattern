# Standardizing Habit Completion Circles

As a Lead UX/UI Designer and Expert Android Developer, I will standardize the appearance of all "Habit Circles" (completion rings) across the app. The goal is to create a cohesive visual language while preserving all existing animations and functional behaviors.

## Proposed Changes

### Core UI Standardization

I will establish a consistent set of dimensions and styling for all habit-related rings:

| Attribute | **Home Screen Rings** (`TaskRing`, `TimerRing`) | **Detail View Rings** (`HabitProgressCard`) |
| :--- | :--- | :--- |
| **Ring Size** | `36.dp` (increased from 34.dp) | `48.dp` (kept as is) |
| **Stroke Width** | `3.5.dp` | `5.dp` (standardized proportion) |
| **Icon Size** | `20.dp` | `24.dp` (if applicable) |
| **Colors** | Dynamic `accentColor` for active progress | Dynamic `accentColor` |
| **Track Alpha** | `0.12f` of accent color | `0.12f` of accent color |
| **Stroke Cap** | `StrokeCap.Round` | `StrokeCap.Round` |

---

### [Component] Home Screen Action Rings

#### [MODIFY] [TimerRing.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/components/TimerRing.kt)
- Update `ringSize` to `36.dp`.
- Update `strokeWidthDp` to `3.5.dp`.
- Update `iconSize` to `20.dp`.
- **CRITICAL**: Update colors to use an `accentColor` parameter (passed from `BaseHabitCard`) instead of hardcoded `MaterialTheme.colorScheme.primary`.
- Update the track color to use the `accentColor` with consistent alpha.

#### [MODIFY] [TaskRing.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/components/TaskRing.kt)
- Update `ringSize` to `36.dp`.
- Update `strokeWidthDp` to `3.5.dp`.
- Update `iconSize` to `20.dp`.
- **CRITICAL**: Update colors to use an `accentColor` parameter instead of `MaterialTheme.colorScheme.onSurface`.
- Align track color and alpha with `TimerRing`.

#### [MODIFY] [HabitBuildCard.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/components/HabitBuildCard.kt)
- Pass `accentColor` from `BaseHabitCard` to `TimerRing`.

#### [MODIFY] [HabitTaskCard.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/components/HabitTaskCard.kt)
- Pass `accentColor` from `BaseHabitCard` to `TaskRing`.

#### [MODIFY] [HabitQuitCard.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/homeScreen/components/HabitQuitCard.kt)
- Pass `accentColor` from `BaseHabitCard` to `TaskRing`.

---

### [Component] Habit Details

#### [MODIFY] [HabitProgressCard.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/components/HabitProgressCard.kt)
- Update `strokeWidth` to `5.dp` to match the boldness profile of the 36dp/3.5dp standard.
- Ensure track alpha is consistent with other rings (`0.12f`).

## Verification Plan

### Automated Tests
- I will run a build to ensure no compilation errors after adding the `accentColor` parameters.
- I will verify that `HabitCardModel` data flows correctly through the components.

### Manual Verification
- Deploy to device/emulator.
- Observe "Grow" habits (Timer) and "Task/Quit" habits on the home screen.
- Verify that both use the exact same ring thickness and size.
- Verify that both use the habit's assigned accent color for the progress ring.
- Check the Habit Detail screen to ensure the mastery circle feels part of the same design system.
