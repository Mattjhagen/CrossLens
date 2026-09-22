package com.crosslens.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        if (value == null) return null
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
