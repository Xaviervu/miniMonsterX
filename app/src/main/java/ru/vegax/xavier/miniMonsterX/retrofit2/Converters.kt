package ru.vegax.xavier.miniMonsterX.retrofit2

import androidx.room.TypeConverter
import com.google.gson.Gson


class Converters {
    @TypeConverter
    fun stringListToJson(value: List<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToStringList(value: String): List<String>? {
        val objects = Gson().fromJson(value, Array<String>::class.java) as Array<String>
        return objects.toList()
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