package ru.vegax.xavier.minimonsterx.retrofit2

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {
    @TypeConverter
    fun stringListToJson(value: List<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToStringList(value: String): List<String>? {
        val objects = Gson().fromJson(value, Array<String>::class.java) as Array<String>
        val list = objects.toList()
        return list
    }
    @TypeConverter
    fun boolListToJson(value: List<Boolean>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToBoolList(value: String): List<Boolean>? {
        val objects = Gson().fromJson(value, Array<Boolean>::class.java) as Array<Boolean>
        val list = objects.toList()
        return list
    }
}