package sdk.sahha.android.source

import androidx.annotation.Keep

private const val IOS_ONLY = "This sensor is only available in iOS, this data type will not be collected"

@Keep
enum class SahhaSensor {
    gender,
    date_of_birth,
    sleep,
    steps,
    floors_climbed,
    heart_rate,
    resting_heart_rate,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_heart_rate_average,

    @Deprecated(
        message = "$IOS_ONLY. Please use heart_rate_variability_rmssd for Android instead",
        replaceWith = ReplaceWith("heart_rate_variability_rmssd"),
        level = DeprecationLevel.WARNING
    )
    heart_rate_variability_sdnn,
    heart_rate_variability_rmssd,
    blood_pressure_systolic,
    blood_pressure_diastolic,
    blood_glucose,
    vo2_max,
    oxygen_saturation,
    respiratory_rate,
    active_energy_burned,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    basal_energy_burned,
    total_energy_burned,
    basal_metabolic_rate,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    time_in_daylight,
    body_temperature,
    basal_body_temperature,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    sleeping_wrist_temperature,
    height,
    weight,
    lean_body_mass,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    body_mass_index,
    body_fat,
    body_water_mass,
    bone_mass,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    waist_circumference,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    stand_time,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    move_time,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    exercise_time,
    activity_summary,
    device_lock,
    exercise,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    running_speed,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    running_power,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    running_ground_contact_time,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    running_stride_length,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    running_vertical_oscillation,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    six_minute_walk_test_distance,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    stair_ascent_speed,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    stair_descent_speed,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_speed,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_steadiness,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_asymmetry_percentage,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_double_support_percentage,

    @Deprecated(
        message = IOS_ONLY,
        level = DeprecationLevel.WARNING
    )
    walking_step_length,
}