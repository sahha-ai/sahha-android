package sdk.sahha.android.framework.mapper

import android.annotation.SuppressLint
import android.health.connect.datatypes.Metadata
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper

internal class HealthConnectConstantsMapperImpl : HealthConnectConstantsMapper {

    @SuppressLint("RestrictedApi")
    override fun sleepStages(constantInt: Int): String? {
        val sleepStagePrefix = "sleep_stage_"

        return when (constantInt) {
            SleepSessionRecord.STAGE_TYPE_AWAKE -> Constants.SLEEP_STAGE_AWAKE_IN_OR_OUT_OF_BED
            else -> sleepStagePrefix + SleepSessionRecord.STAGE_TYPE_INT_TO_STRING_MAP[constantInt]
        }
    }

    override fun devices(constantInt: Int?): String {
        return when (constantInt) {
            Device.TYPE_WATCH -> "watch"
            Device.TYPE_PHONE -> "phone"
            Device.TYPE_SCALE -> "scale"
            Device.TYPE_RING -> "ring"
            Device.TYPE_HEAD_MOUNTED -> "head_mounted"
            Device.TYPE_FITNESS_BAND -> "fitness_band"
            Device.TYPE_CHEST_STRAP -> "chest_strap"
            Device.TYPE_SMART_DISPLAY -> "smart_display"
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

    @SuppressLint("RestrictedApi")
    override fun mealType(constantInt: Int): String? {
        return MealType.MEAL_TYPE_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun relationToMeal(constantInt: Int): String? {
        return BloodGlucoseRecord.RELATION_TO_MEAL_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun specimenSource(constantInt: Int): String? {
        return BloodGlucoseRecord.SPECIMEN_SOURCE_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun bodyPosition(constantInt: Int): String? {
        return BloodPressureRecord.BODY_POSITION_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun measurementLocation(constantInt: Int): String? {
        return BloodPressureRecord.MEASUREMENT_LOCATION_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun bodyTempMeasurementLocation(constantInt: Int): String? {
        return BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun measurementMethod(constantInt: Int): String? {
        return Vo2MaxRecord.MEASUREMENT_METHOD_INT_TO_STRING_MAP[constantInt]
    }

    @SuppressLint("RestrictedApi")
    override fun exerciseTypes(constantInt: Int): String? {
        return ExerciseSessionRecord.EXERCISE_TYPE_INT_TO_STRING_MAP[constantInt]
    }

    override fun exerciseSegments(constantInt: Int): String? {
        val segmentTypeStringToIntMap = mapOf(
            "unknown" to 0,
            "arm_curl" to 1,
            "back_extension" to 2,
            "ball_slam" to 3,
            "barbell_shoulder_press" to 4,
            "bench_press" to 5,
            "bench_sit_up" to 6,
            "biking" to 7,
            "biking_stationary" to 8,
            "burpee" to 9,
            "crunch" to 10,
            "deadlift" to 11,
            "double_arm_triceps_extension" to 12,
            "dumbbell_curl_left_arm" to 13,
            "dumbbell_curl_right_arm" to 14,
            "dumbbell_front_raise" to 15,
            "dumbbell_lateral_raise" to 16,
            "dumbbell_row" to 17,
            "dumbbell_triceps_extension_left_arm" to 18,
            "dumbbell_triceps_extension_right_arm" to 19,
            "dumbbell_triceps_extension_two_arm" to 20,
            "elliptical" to 21,
            "forward_twist" to 22,
            "front_raise" to 23,
            "high_intensity_interval_training" to 24,
            "hip_thrust" to 25,
            "hula_hoop" to 26,
            "jumping_jack" to 27,
            "jump_rope" to 28,
            "kettlebell_swing" to 29,
            "lateral_raise" to 30,
            "lat_pull_down" to 31,
            "leg_curl" to 32,
            "leg_extension" to 33,
            "leg_press" to 34,
            "leg_raise" to 35,
            "lunge" to 36,
            "mountain_climber" to 37,
            "other_workout" to 38,
            "pause" to 39,
            "pilates" to 40,
            "plank" to 41,
            "pull_up" to 42,
            "punch" to 43,
            "rest" to 44,
            "rowing_machine" to 45,
            "running" to 46,
            "running_treadmill" to 47,
            "shoulder_press" to 48,
            "single_arm_triceps_extension" to 49,
            "sit_up" to 50,
            "squat" to 51,
            "stair_climbing" to 52,
            "stair_climbing_machine" to 53,
            "stretching" to 54,
            "swimming_backstroke" to 55,
            "swimming_breaststroke" to 56,
            "swimming_butterfly" to 57,
            "swimming_freestyle" to 58,
            "swimming_mixed" to 59,
            "swimming_open_water" to 60,
            "swimming_other" to 61,
            "swimming_pool" to 62,
            "upper_twist" to 63,
            "walking" to 64,
            "weightlifting" to 65,
            "wheelchair" to 66,
            "yoga" to 67,
        )
        val segmentTypeIntToStringMap =
            segmentTypeStringToIntMap.entries.associateBy({ it.value }, { it.key })

        return segmentTypeIntToStringMap[constantInt]
    }
}
