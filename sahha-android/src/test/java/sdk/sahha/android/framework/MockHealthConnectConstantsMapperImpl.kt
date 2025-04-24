package sdk.sahha.android.framework

import android.health.connect.datatypes.Metadata
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper

class MockHealthConnectConstantsMapperImpl: HealthConnectConstantsMapper {
    override fun devices(constantInt: Int?): String {
        return when (constantInt) {
            Device.TYPE_WATCH -> "WATCH"
            Device.TYPE_PHONE -> "PHONE"
            Device.TYPE_SCALE -> "SCALE"
            Device.TYPE_RING -> "RING"
            Device.TYPE_HEAD_MOUNTED -> "HEAD_MOUNTED"
            Device.TYPE_FITNESS_BAND -> "FITNESS_BAND"
            Device.TYPE_CHEST_STRAP -> "CHEST_STRAP"
            Device.TYPE_SMART_DISPLAY -> "SMART_DISPLAY"
            else -> Constants.UNKNOWN
        }
    }

    override fun recordingMethod(constantInt: Int): String {
        return when (constantInt) {
            Metadata.RECORDING_METHOD_ACTIVELY_RECORDED -> RecordingMethods.actively_recorded.name
            Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED -> RecordingMethods.automatically_recorded.name
            Metadata.RECORDING_METHOD_MANUAL_ENTRY -> RecordingMethods.manual_entry.name
            else -> RecordingMethods.unknown.name
        }
    }

    override fun mealType(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun relationToMeal(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun specimenSource(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun bodyPosition(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun measurementLocation(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun sleepStages(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun measurementMethod(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun bodyTempMeasurementLocation(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun exerciseTypes(constantInt: Int): String? {
        TODO("Not yet implemented")
    }

    override fun exerciseSegments(constantInt: Int): String? {
        TODO("Not yet implemented")
    }
}