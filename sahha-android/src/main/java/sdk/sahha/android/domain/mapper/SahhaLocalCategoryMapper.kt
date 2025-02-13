package sdk.sahha.android.domain.mapper

import sdk.sahha.android.domain.internal_enum.SahhaLocalCategory
import sdk.sahha.android.source.SahhaSensor

internal val SahhaSensor.category: SahhaLocalCategory
    get() = when (this) {
        SahhaSensor.gender,
        SahhaSensor.date_of_birth,
            -> SahhaLocalCategory.CHARACTERISTIC

        SahhaSensor.sleep ->
            SahhaLocalCategory.SLEEP

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
            -> SahhaLocalCategory.ACTIVITY

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
            -> SahhaLocalCategory.VITALS

        SahhaSensor.height,
        SahhaSensor.weight,
        SahhaSensor.body_fat,
        SahhaSensor.lean_body_mass,
        SahhaSensor.body_water_mass,
        SahhaSensor.bone_mass,
        SahhaSensor.body_mass_index,
        SahhaSensor.waist_circumference,
            -> SahhaLocalCategory.BODY

        SahhaSensor.device_lock
            -> SahhaLocalCategory.DEVICE

        SahhaSensor.exercise
            -> SahhaLocalCategory.EXERCISE
    }