package com.example.pattern.data.local.converter

import androidx.room.TypeConverter
import com.example.pattern.data.local.entity.HabitType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Provides methods to convert complex data types into formats Room can store (and back).
class Converters {
    @TypeConverter
    fun fromBooleanList(list: List<Boolean>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toBooleanList(json: String): List<Boolean> {
        val type = object : TypeToken<List<Boolean>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromHabitType(type: HabitType): String {
        return type.name
    }

    @TypeConverter
    fun toHabitType(name: String): HabitType {
        return HabitType.valueOf(name)
    }
}