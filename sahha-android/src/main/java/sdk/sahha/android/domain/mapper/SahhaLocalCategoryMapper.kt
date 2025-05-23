package sdk.sahha.android.domain.mapper

import sdk.sahha.android.source.SahhaBiomarkerCategory
import sdk.sahha.android.source.SahhaSensor

// Helps assign the appropriate category to the sensor types
internal val SahhaSensor.category: SahhaBiomarkerCategory
    get() = when (this) {
        SahhaSensor.gender,
        SahhaSensor.date_of_birth,
            -> SahhaBiomarkerCategory.characteristic

        SahhaSensor.sleep ->
            SahhaBiomarkerCategory.sleep

        SahhaSensor.steps,
        SahhaSensor.floors_climbed,
        SahhaSensor.active_energy_burned,
        SahhaSensor.total_energy_burned,
        SahhaSensor.basal_metabolic_rate,
        SahhaSensor.activity_summary,
        SahhaSensor.basal_energy_burned,
        SahhaSensor.time_in_daylight,
        SahhaSensor.stand_time,
        SahhaSensor.move_time,
        SahhaSensor.exercise_time,
        SahhaSensor.running_speed,
        SahhaSensor.running_power,
        SahhaSensor.running_ground_contact_time,
        SahhaSensor.running_stride_length,
        SahhaSensor.running_vertical_oscillation,
        SahhaSensor.six_minute_walk_test_distance,
        SahhaSensor.stair_ascent_speed,
        SahhaSensor.stair_descent_speed,
        SahhaSensor.walking_speed,
        SahhaSensor.walking_steadiness,
        SahhaSensor.walking_asymmetry_percentage,
        SahhaSensor.walking_double_support_percentage,
        SahhaSensor.walking_step_length,
            -> SahhaBiomarkerCategory.activity

        SahhaSensor.heart_rate,
        SahhaSensor.resting_heart_rate,
        SahhaSensor.heart_rate_variability_rmssd,
        SahhaSensor.respiratory_rate,
        SahhaSensor.oxygen_saturation,
        SahhaSensor.vo2_max,
        SahhaSensor.blood_glucose,
        SahhaSensor.blood_pressure_systolic,
        SahhaSensor.blood_pressure_diastolic,
        SahhaSensor.body_temperature,
        SahhaSensor.basal_body_temperature,
        SahhaSensor.heart_rate_variability_sdnn,
        SahhaSensor.walking_heart_rate_average,
        SahhaSensor.sleeping_wrist_temperature,
            -> SahhaBiomarkerCategory.vitals

        SahhaSensor.height,
        SahhaSensor.weight,
        SahhaSensor.body_fat,
        SahhaSensor.lean_body_mass,
        SahhaSensor.body_water_mass,
        SahhaSensor.bone_mass,
        SahhaSensor.body_mass_index,
        SahhaSensor.waist_circumference,
            -> SahhaBiomarkerCategory.body

        SahhaSensor.device_lock
            -> SahhaBiomarkerCategory.device

        SahhaSensor.exercise
            -> SahhaBiomarkerCategory.exercise
    }