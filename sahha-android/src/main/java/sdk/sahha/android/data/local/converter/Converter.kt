package sdk.sahha.android.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

// Converter for Room Database to handle JSON
internal class Converter {
    private val gson = Gson()

    @TypeConverter
    fun fromJsonString(value: String?): ArrayList<Int?>? {
        val listType: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Int?>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromJsonStringToHashMap(value: String?): HashMap<String, String>? {
        val type: Type = object : TypeToken<HashMap<String, String>?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromHashMapToJsonString(hashMap: HashMap<String, String>?): String? {
        return gson.toJson(hashMap)
    }
}