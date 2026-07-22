# Implementation Plan - Professional UI/UX Overhaul for Settings

This plan transforms the `SettingsScreen` into a professional, clean, and stylish interface following modern Material 3 design trends. The focus is on typography, balanced spacing, and refined interactive elements.

## Proposed Changes

### UI Components Refinement

#### [MODIFY] [SettingsScreen.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/screens/settings/SettingsScreen.kt)
- **Top Bar Enhancement**:
    - Update `CenterAlignedTopAppBar` to use `titleMedium` or `titleLarge` with refined weight.
    - Improve the back button icon and touch target.
- **Section Styling**:
    - Use `surfaceContainerLow` for section backgrounds for a softer, more professional depth.
    - Increase rounded corners to `28.dp` for a modern "pill" or "soft-card" look.
    - Add `Modifier.animateContentSize()` to sections for smooth transitions when quiet hours items are toggled.
- **Navigation & Switch Items**:
    - **Icons**: Use a more subtle background (e.g., `surfaceVariant` with `0.4f` alpha) for icon circles.
    - **Typography**: Shift to `titleSmall` for item titles and `bodySmall` for subtitles to create better hierarchy.
    - **Interaction**: Add a subtle trailing arrow (chevron) to `SettingsNavigationItem` to clearly indicate navigation.
    - **Dividers**: Make dividers even more subtle by reducing alpha or using `outlineVariant`.
- **Spacing**:
    - Increase vertical spacing between sections to `24.dp`.
    - Increase internal padding within items for a more "breathable" design.

#### [MODIFY] [SectionHeader.kt](file:///Users/emreuyar/AndroidStudioProjects/pattern/app/src/main/java/com/example/pattern/ui/components/SectionHeader.kt)
- Refine the `SectionHeader` to be slightly smaller and more elegant, perhaps using `labelMedium` with higher letter spacing.

---

### Layout & Animations
- Ensure `LazyColumn` has balanced `contentPadding`.
- Add subtle fade-in animations if possible (within the scope of "clean and nice").

## Verification Plan

### Manual Verification
1. **Visual Consistency**: Check if all sections follow the new "soft-card" design.
2. **Typography Check**: Ensure titles are distinct from subtitles and headers.
3. **Interactivity**: Verify that clicking items feels responsive and the chevron icons are correctly aligned.
4. **Toggling**: Enable/Disable "Quiet Hours" and verify the smooth animation of the expanding/collapsing list.
5. **Night Mode**: Test the UI in Dark Theme to ensure the `surfaceContainerLow` and icon backgrounds adapt perfectly.
