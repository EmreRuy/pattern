package com.example.pattern.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalTime

enum class TimePeriod(val displayName: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night");

    val icon: ImageVector
        get() = when (this) {
            MORNING -> Icons.Rounded.WbTwilight
            AFTERNOON -> Icons.Rounded.LightMode
            EVENING -> Icons.Rounded.WbCloudy
            NIGHT -> Icons.Rounded.Bedtime
        }
}

object TimeUtils {
    fun getCurrentTimePeriod(time: LocalTime = LocalTime.now()): TimePeriod {
        return when (time.hour) {
            in 5..11 -> TimePeriod.MORNING
            in 12..16 -> TimePeriod.AFTERNOON
            in 17..20 -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }
}
