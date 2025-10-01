package com.example.pattern.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Provides methods to convert complex data types into formats Room can store (and back).
class Converters {

    // Converts the List<Boolean> (for selected days) to a JSON string for storage
    @TypeConverter
    fun fromBooleanList(list: List<Boolean>): String {
        return Gson().toJson(list)
    }

    // Converts the JSON string back into a List<Boolean> when reading from the database
    @TypeConverter
    fun toBooleanList(json: String): List<Boolean> {
        val type = object : TypeToken<List<Boolean>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Converts the HabitType enum to its String name for storage
    @TypeConverter
    fun fromHabitType(type: HabitType): String {
        return type.name
    }

    // Converts the String back into the HabitType enum
    @TypeConverter
    fun toHabitType(name: String): HabitType {
        return HabitType.valueOf(name)
    }
}