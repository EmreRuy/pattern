package com.example.pattern.data.local.converter

import androidx.room.TypeConverter
import com.example.pattern.data.local.entity.HabitType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Optimized TypeConverters for Room.
 * Uses a single Gson instance to avoid redundant allocations.
 */
class Converters {
    private val gson = Gson()
    private val booleanListType = object : TypeToken<List<Boolean>>() {}.type

    @TypeConverter
    fun fromBooleanList(list: List<Boolean>?): String {
        return gson.toJson(list ?: emptyList<Boolean>())
    }

    @TypeConverter
    fun toBooleanList(json: String?): List<Boolean> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            gson.fromJson(json, booleanListType)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromHabitType(type: HabitType): String {
        return type.name
    }

    @TypeConverter
    fun toHabitType(name: String): HabitType {
        return try {
            HabitType.valueOf(name)
        } catch (_: Exception) {
            HabitType.BUILD // Default fallback
        }
    }
}
