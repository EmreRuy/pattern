package com.example.pattern.data.model

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.SettingsEntity
import com.google.gson.annotations.SerializedName

data class BackupDto(
    @SerializedName("habits")
    val habits: List<Habit>,
    @SerializedName("daily_states")
    val dailyStates: List<HabitDailyState>,
    @SerializedName("settings")
    val settings: SettingsEntity?
)
