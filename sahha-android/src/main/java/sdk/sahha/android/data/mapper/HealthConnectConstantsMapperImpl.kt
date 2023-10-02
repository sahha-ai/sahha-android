package sdk.sahha.android.data.mapper

import android.health.connect.datatypes.Metadata
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.DeviceTypes
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper

class HealthConnectConstantsMapperImpl : HealthConnectConstantsMapper {
    override fun devices(constantInt: Int?): String {
        return when (constantInt) {
            Device.TYPE_WATCH -> DeviceTypes.WATCH
            Device.TYPE_PHONE -> DeviceTypes.PHONE
            Device.TYPE_SCALE -> DeviceTypes.SCALE
            Device.TYPE_RING -> DeviceTypes.RING
            Device.TYPE_HEAD_MOUNTED -> DeviceTypes.HEAD_MOUNTED
            Device.TYPE_FITNESS_BAND -> DeviceTypes.FITNESS_BAND
            Device.TYPE_CHEST_STRAP -> DeviceTypes.CHEST_STRAP
            Device.TYPE_SMART_DISPLAY -> DeviceTypes.SMART_DISPLAY
            else -> DeviceTypes.UNKNOWN
        }
    }

    override fun recordingMethod(constantInt: Int): String {
        return when (constantInt) {
            Metadata.RECORDING_METHOD_ACTIVELY_RECORDED -> RecordingMethodsHealthConnect.RECORDING_METHOD_ACTIVELY_RECORDED.name
            Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED -> RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name
            Metadata.RECORDING_METHOD_MANUAL_ENTRY -> RecordingMethodsHealthConnect.RECORDING_METHOD_MANUAL_ENTRY.name
            else -> RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name
        }
    }

    override fun mealType(constantInt: Int): String? {
        return MealType.MEAL_TYPE_INT_TO_STRING_MAP[constantInt]
    }

    override fun relationToMeal(constantInt: Int): String? {
        return BloodGlucoseRecord.RELATION_TO_MEAL_INT_TO_STRING_MAP[constantInt]
    }

    override fun specimenSource(constantInt: Int): String? {
        return BloodGlucoseRecord.SPECIMEN_SOURCE_INT_TO_STRING_MAP[constantInt]
    }

    override fun bodyPosition(constantInt: Int): String? {
        return BloodPressureRecord.BODY_POSITION_INT_TO_STRING_MAP[constantInt]
    }

    override fun measurementLocation(constantInt: Int): String? {
        return BloodPressureRecord.MEASUREMENT_LOCATION_INT_TO_STRING_MAP[constantInt]
    }
}
