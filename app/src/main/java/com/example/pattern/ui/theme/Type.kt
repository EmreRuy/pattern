package com.example.pattern.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.example.pattern.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Lato"),
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baseline = Typography()

// Optimized TextStyle with PlatformSettings to prevent layout shifts
private val defaultPlatformStyle = PlatformTextStyle(
    includeFontPadding = false
)

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily, platformStyle = defaultPlatformStyle),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily, platformStyle = defaultPlatformStyle),
)
