package sdk.sahha.android.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

// Converter for Room Database to handle JSON
internal class Converter {
    @TypeConverter
    fun fromJsonString(value: String?): ArrayList<Int?>? {
        val listType: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Int?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}