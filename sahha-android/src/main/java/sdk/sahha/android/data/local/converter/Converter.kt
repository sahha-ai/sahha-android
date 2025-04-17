package sdk.sahha.android.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import sdk.sahha.android.domain.model.metadata.SahhaMetadata
import java.lang.reflect.Type

// Converter for Room Database to handle JSON
internal class Converter {
    private val gson = Gson()

    @TypeConverter
    fun fromJsonStringInt(value: String?): ArrayList<Int?>? {
        val listType: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayListInt(list: ArrayList<Int?>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromJsonStringString(value: String?): ArrayList<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayListString(list: ArrayList<String?>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromJsonStringToHashMap(value: String?): HashMap<String, String>? {
        val type: Type = object : TypeToken<HashMap<String, Any>?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromHashMapToJsonString(hashMap: HashMap<String, String>?): String? {
        return gson.toJson(hashMap)
    }

    @TypeConverter
    fun fromJsonStringToSahhaMetadata(value: String?): SahhaMetadata? {
        return value?.let { gson.fromJson(value, SahhaMetadata::class.java) }
    }

    @TypeConverter
    fun fromHashMapAnyToJsonString(metadata: SahhaMetadata?): String? {
        return gson.toJson(metadata)
    }
}