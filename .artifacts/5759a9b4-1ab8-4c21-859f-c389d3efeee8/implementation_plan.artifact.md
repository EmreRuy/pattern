# Implementation Plan - Smooth Language Transition

This plan addresses the "screen blink" when changing the app language. The blink is primarily caused by the Activity recreation required to apply new resources, which triggers the Splash Screen to appear again.

## Proposed Changes

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/MainActivity.kt)
- **Skip Splash on Recreation**: Modify the `setKeepOnScreenCondition` to return `false` if `savedInstanceState` is not null. This prevents the Splash Screen from showing up when the activity is recreated due to a language change or configuration change.
- **Top-level Crossfade**: Wrap the `MainContent` in an `AnimatedContent` or `Crossfade` (optional, if the splash skip isn't enough).

#### [MODIFY] [AndroidManifest.xml](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/AndroidManifest.xml)
- **Handle Config Changes**: Add `android:configChanges="locale|layoutDirection|localeConfig"` to `MainActivity`. On Android 13+, this may allow the system to apply locale changes without a full Activity recreation if handled correctly.

---

### Logic / Navigation

#### [MODIFY] [MainViewModel.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/MainViewModel.kt)
- Ensure that the UI state is restored as quickly as possible upon recreation.

---

## Verification Plan

### Manual Verification
1. Open Settings -> Language.
2. Select "Turkish".
3. Verify that the screen transitions smoothly without a visible "blink" or the Splash Screen reappearing.
4. Verify that the UI correctly updates to Turkish.
5. Test on both a device with Android 13+ and an older device (e.g., API 30) to ensure consistency.
