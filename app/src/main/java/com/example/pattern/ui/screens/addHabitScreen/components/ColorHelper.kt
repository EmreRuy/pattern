package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.ui.graphics.Color

fun blendColors(base: Color, overlay: Color, ratio: Float): Color {
    val inverse = 1f - ratio
    return Color(
        red = base.red * inverse + overlay.red * ratio,
        green = base.green * inverse + overlay.green * ratio,
        blue = base.blue * inverse + overlay.blue * ratio,
        alpha = 1f
    )
}