# Sahha SDK for Android Apps

The Sahha SDK provides a convenient way for Android apps to connect to the Sahha API.

For more information on Sahha please visit https://sahha.ai

---

## Docs

The Sahha Docs provide detailed instructions for installation and usage of the Sahha SDK.

[Sahha Docs](https://docs.sahha.ai)

---

## Example

The Sahha Demo App provides a convenient way to try the features of the Sahha SDK.

[Sahha Demo App](https://github.com/sahha-ai/sahha-demo-android)

---

## Health Data Source Integrations

Sahha supports integration with the following health data sources:

- [Google Fit](https://sahha.notion.site/Google-Fit-131b2f553bbf804a8ee6fef7bc1f4edb?pvs=25)
- [Samsung Health](https://sahha.notion.site/Samsung-Health-d3f76840fad142469f5e724a54c24ead?pvs=25)
- [Fitbit](https://sahha.notion.site/Fitbit-12db2f553bbf809fa93ff01f9acd7330?pvs=25)
- [Garmin Connect](https://sahha.notion.site/Garmin-12db2f553bbf80afb916d04a62e857e6?pvs=25)
- [Polar Flow](https://sahha.notion.site/Polar-12db2f553bbf80c3968eeeab55b484a2?pvs=25)
- [Withings Health Mate](https://sahha.notion.site/Withings-12db2f553bbf80a38d31f80ab083613f?pvs=25)
- [Oura Ring](https://sahha.notion.site/Oura-12db2f553bbf80cf96f2dfd8343b4f06?pvs=25)
- [Whoop](https://sahha.notion.site/WHOOP-12db2f553bbf807192a5c69071e888f4?pvs=25)
- [Strava](https://sahha.notion.site/Strava-12db2f553bbf80c48312c2bf6aa5ac65?pvs=25)
- [Sleep as Android](https://sahha.notion.site/Sleep-as-Android-Smart-alarm-131b2f553bbf802eb7e4dca6baab1049?pvs=25)

& many more! Please visit our [integrations](https://sahha.notion.site/data-integrations?v=17eb2f553bbf80e0b0b3000c0983ab01) page for more information

---

## Install

In the **app** level `build.gradle` file
```groovy
dependencies {
  implementation 'ai.sahha:sahha-android:1.1.4'
}
```


In the `AndroidManifest.xml` file, declare Google Health Connect data types if required, e.g. Sleep and step count.

More data types are available such as heart rate, workout / exercise, please refer to the links below for more information.

```xml
<!-- Sleep -->
<uses-permission android:name="android.permission.health.READ_SLEEP" />

<!-- Activity -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_FLOORS_CLIMBED" />
```

This is recommended if you'd like to retrieve Health Connect data from other health apps such as WHOOP, Garmin, Samsung Health etc.

To declare other sensor permissions, please refer to [this](https://docs.sahha.ai/docs/data-flow/sdk/setup#step-2-review-sensor-permissions) page.

Only include the sensor permissions required by your project, what is declared here will be reviewed by the Play Store.

You must be able to justify reasons behind requiring the sensor permissions, [these](https://docs.sahha.ai/docs/data-flow/sdk/app-store-submission/google-play-store#data-type-justifications) justifications may be used to clearly articulate the reasoning behind your required sensor permissions.

---

## API

<docgen-index>

* [`configure(...)`](#configure)
* [`isAuthenticated`](#isauthenticated)
* [`authenticate()`](#authenticate)
* [`deauthenticate()`](#deauthenticate)
* [`profileToken`](#profiletoken)
* [`getDemographic()`](#getdemographic)
* [`postDemographic(...)`](#postdemographic)
* [`getSensorStatus(...)`](#getsensorstatus)
* [`enableSensors(...)`](#enablesensors)
* [`getScores(...)`](#getscores)
* [`getBiomarkers(...)`](#getbiomarkers)
* [`getStats(...)`](#getstats)
* [`getSamples(...)`](#getsamples)
* [`openAppSettings()`](#openappsettings)
* [Interfaces](#interfaces)
* [Enums](#enums)

</docgen-index>

<docgen-api>

### configure(...)

```kotlin
fun configure(
  activity: ComponentActivity,
  sahhaSettings: SahhaSettings,
  callback: ((error: String?, success: Boolean) -> Unit)? = null
)
```

**Example usage**:
```kotlin
// In MainActivity

val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)

// Without optional callback
Sahha.configure(this, settings)

// With optional callback
Sahha.configure(this, settings) { error: String?, success: Boolean ->
  error?.also { e -> Log.e(TAG, e) }
  Log.d(TAG, success)
}
```

---

### isAuthenticated

```kotlin
val isAuthenticated: Boolean
```

**Example usage**:
```kotlin
if (Sahha.isAuthenticated == false) {
  // E.g. Authenticate the user
}
```

---

### authenticate(...)

```kotlin
fun authenticate(
  appId: String,
  appSecret: String,
  externalId: String,
  callback: (error: String?, success: boolean) -> Unit
)

//or

fun authenticate(
    profileToken: String,
    refreshToken: String,
    callback: (error: String?, success: Boolean) -> Unit
)
```

**Example usage**:
```kotlin
Sahha.authenticate(
  APP_ID,
  APP_SECRET,
  EXTERNAL_ID, // Some unique identifier for the user
) { error: String?, success: Boolean ->
  if (success) {
    // E.g. Continue onboarding flow
  } else {
    error?.also { e -> Log.e(TAG, e) }
  }
}
```

---

### deauthenticate()

```kotlin
fun deauthenticate(
    callback: (suspend (error: String?, success: Boolean) -> Unit)
)
```

**Example usage**:
```kotlin
deauthenticate { error: String?, success: Boolean ->
    if (success) {
        // E.g. Continue logic for successful deauthentication
    } else {
        error?.also { e -> Log.e(TAG, e) }
    }
}
```

---

### profileToken

```kotlin
val profileToken: String?
```

**Example usage**:
```kotlin
val httpHeader = Sahha.profileToken?.let { token: String ->
    mapOf(Pair("Authorization", token))
}

ExampleWebView(
    httpHeader = httpHeader,
    url = WEBVIEW_URL
)
```

---

### getDemographic()

```kotlin
fun getDemographic(
    callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?
)
```

**Example usage**
```kotlin
Sahha.getDemographic { error: String?, demographic: SahhaDemographic? ->
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, demographic)
}
```

---

### postDemographic()

```kotlin
fun postDemographic(
    sahhaDemographic: SahhaDemographic,
    callback: ((error: String?, success: Boolean) -> Unit)?
)
```

**Example usage**:
```kotlin
val age = 123
val gender = "Male"
val demographic = SahhaDemographic(
    age = age,
    gender = gender
)

Sahha.postDemographic(demographic) { error: String?, success: Boolean ->
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, success)
}
```

---

### getSensorStatus(...)

```kotlin
fun getSensorStatus(
    context: Context,
    sensors: Set<SahhaSensor>,
    callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
)
```

**Example usage**:
```kotlin
val sensors = setOf(
    SahhaSensor.steps,
    SahhaSensor.sleep,
)

Sahha.getSensorStatus(
    context = context,
    sensors = sensors,
) { error: String?, status: Enum<SahhaSensorStatus> ->
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, status.name)
}
```

---

### enableSensors(...)

```kotlin
fun enableSensors(
    context: Context,
    sensors: Set<SahhaSensor>,
    callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
)
```

**Example usage**:
```kotlin
val sensors = setOf(
    SahhaSensor.steps,
    SahhaSensor.sleep,
)

Sahha.enableSensors(
    context = context,
    sensors = sensors,
) { error: String?, status: Enum<SahhaSensorStatus> ->
    error?.also { e -> Log.e(TAG, e) }
    
    if (status == SahhaSensorStatus.unavailable) {
        // E.g. Inform user the sensors are unavailable  
    } else if (status == SahhaSensorStatus.enabled || status == SahhaSensorStatus.disabled) { // enabled or disabled
        // E.g. Continue flow (disabled can mean some permissions were granted and some were not (partially granted))
    }
}
```

---

### getScores(...)

```kotlin
fun getScores(
    types: Set<SahhaScoreType>,
    callback: ((error: String?, value: String?) -> Unit)?
)

// or specify dates

fun getScores(
    types: Set<SahhaScoreType>,
    dates: Pair<LocalDateTime, LocalDateTime>,
    callback: ((error: String?, value: String?) -> Unit)?
)

// or

fun getScores(
    types: Set<SahhaScoreType>,
    dates: Pair<Date, Date>,
    callback: ((error: String?, value: String?) -> Unit)?
)
```

**Example usage**:
```kotlin
val types = setOf(
    SahhaScoreType.activity,
    SahhaScoreType.sleep,
)

// E.g. Last 2 weeks
Sahha.getScores(
    types = types,
    dates = Pair(
        LocalDateTime.now().minusDays(14), // start date time
        LocalDateTime.now() // end date time
    )
) { error: String?, value: String? ->
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, value) // value is in the form of a JSON array
}
```

---

### getBiomarkers(...)

```kotlin
fun getBiomarkers(
    categories: Set<SahhaBiomarkerCategory>,
    types: Set<SahhaBiomarkerType>,
    callback: ((error: String?, value: String?) -> Unit)?
)

// or specify dates

fun getBiomarkers(
    categories: Set<SahhaBiomarkerCategory>,
    types: Set<SahhaBiomarkerType>,
    dates: Pair<LocalDateTime, LocalDateTime>,
    callback: ((error: String?, value: String?) -> Unit)?
)

// or

fun getBiomarkers(
    categories: Set<SahhaBiomarkerCategory>,
    types: Set<SahhaBiomarkerType>,
    dates: Pair<Date, Date>,
    callback: ((error: String?, value: String?) -> Unit)?
)
```

**Example usage**:

```kotlin
val categories = setOf(
    SahhaBiomarkerCategory.activity,
    SahhaBiomarkerCategory.sleep
)

val types = setOf(
    SahhaBiomarkerType.steps,
    SahhaBiomarkerType.sleep_start_time,
    SahhaBiomarkerType.sleep_end_time,
    SahhaBiomarkerType.sleep_duration,
)

// E.g. Last 2 weeks
Sahha.getBiomarkers(
    categories = categories,
    types = types,
    dates = Pair(
        LocalDateTime.now().minusDays(14), // start date time
        LocalDateTime.now() // end date time
    )
) { error: String?, value: String? ->
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, value) // value is in the form of a JSON array
}
```

---

### getStats(...)

```kotlin
fun getStats(
    sensor: SahhaSensor,
    dates: Pair<LocalDateTime, LocalDateTime>,
    callback: (error: String?, stats: List<SahhaStat>?) -> Unit
)

// or

fun getStats(
    sensor: SahhaSensor,
    dates: Pair<Date, Date>,
    callback: (error: String?, stats: List<SahhaStat>?) -> Unit
) 
```

**Example usage**:

```kotlin
Sahha.getStats(
    SahhaSensor.steps,
    Pair(
        LocalDateTime.now().minusDays(14), // start date time
        LocalDateTime.now() // end date time
    )
) { error: String?, stats: List<SahhaStat>? -> 
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, stats) // stats are returned as a list of SahhaStat object
}
```

---

### getSamples(...)

```kotlin
fun getSamples(
    sensor: SahhaSensor,
    dates: Pair<LocalDateTime, LocalDateTime>,
    callback: (error: String?, samples: List<SahhaSample>?) -> Unit
)

// or

fun getSamples(
    sensor: SahhaSensor,
    dates: Pair<Date, Date>,
    callback: (error: String?, samples: List<SahhaSample>?) -> Unit
) 
```

**Example usage**:

```kotlin
Sahha.getSamples(
    SahhaSensor.steps,
    Pair(
        LocalDateTime.now().minusDays(14), // start date time
        LocalDateTime.now() // end date time
    )
) { error: String?, samples: List<SahhaSample>? -> 
    error?.also { e -> Log.e(TAG, e) }
    Log.d(TAG, samples) // samples are returned as a list of SahhaSample object
}
```

---

### openAppSettings()

```kotlin
fun openAppSettings()
```

**Example usage**:
```kotlin
// This method is useful when the user denies permissions multiple times -- where the prompt will no longer show
if (status == SahhaSensorStatus.disabled) {
    Sahha.openAppSettings()
}
```

---

### Interfaces

#### SahhaSettings

```kotlin
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val notificationSettings: SahhaNotificationConfiguration?,
    val framework: SahhaFramework,
)
```

#### SahhaDemographic

```kotlin
data class SahhaDemographic(
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?,
    val ethnicity: String?,
    val occupation: String?,
    val industry: String?,
    val incomeRange: String?,
    val education: String?,
    val relationship: String?,
    val locale: String?,
    val livingArrangement: String?,
    val birthDate: String?
)
```

#### SahhaStat

```kotlin
data class SahhaStat(
    val id: String,
    val category: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime,
    val sources: List<String>,
)
```

#### SahhaSample

```kotlin
data class SahhaSample(
    val id: String,
    val category: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime,
    val recordingMethod: String,
    val source: String,
    val stats: List<SahhaStat>
)
```

### Enums

#### SahhaEnvironment

```kotlin
enum class SahhaEnvironment {
    sandbox,
    production
}
```


#### SahhaSensor

```kotlin
enum class SahhaSensor {
    gender,
    date_of_birth,
    sleep,
    steps,
    floors_climbed,
    heart_rate,
    resting_heart_rate,
    walking_heart_rate_average, // iOS only
    heart_rate_variability_sdnn, // iOS only
    heart_rate_variability_rmssd,
    blood_pressure_systolic,
    blood_pressure_diastolic,
    blood_glucose,
    vo2_max,
    oxygen_saturation,
    respiratory_rate,
    active_energy_burned,
    basal_energy_burned, // iOS only
    total_energy_burned,
    basal_metabolic_rate,
    time_in_daylight, // iOS only
    body_temperature,
    basal_body_temperature,
    sleeping_wrist_temperature, // iOS only
    height,
    weight,
    lean_body_mass,
    body_mass_index, // iOS only
    body_fat,
    body_water_mass,
    bone_mass,
    waist_circumference, // iOS only
    stand_time, // iOS only
    move_time, // iOS only
    exercise_time, // iOS only
    activity_summary,
    device_lock,
    exercise,
}
```

#### SahhaSensorStatus

```kotlin
enum class SahhaSensorStatus {
    pending,
    unavailable,
    disabled,
    enabled,
}
```


#### SahhaScoreType

```kotlin
enum class SahhaScoreType {
    wellbeing,
    activity,
    sleep,
    readiness,
    mental_wellbeing,
}
```


#### SahhaBiomarkerCategory

```kotlin
enum class SahhaBiomarkerCategory {
    activity,
    body,
    characteristic,
    reproductive,
    sleep,
    vitals,
    device,
    exercise,
}
```

#### SahhaBiomarkerType

```kotlin
enum class SahhaBiomarkerType {
    steps,
    floors_climbed,
    active_hours,
    active_duration,
    activity_low_intensity_duration,
    activity_mid_intensity_duration,
    activity_high_intensity_duration,
    activity_sedentary_duration,
    active_energy_burned,
    total_energy_burned,
    height,
    weight,
    body_mass_index,
    body_fat,
    fat_mass,
    lean_mass,
    waist_circumference,
    resting_energy_burned,
    age,
    biological_sex,
    date_of_birth,
    menstrual_cycle_length,
    menstrual_cycle_start_date,
    menstrual_cycle_end_date,
    menstrual_phase,
    menstrual_phase_start_date,
    menstrual_phase_end_date,
    menstrual_phase_length,
    sleep_start_time,
    sleep_end_time,
    sleep_duration,
    sleep_debt,
    sleep_interruptions,
    sleep_in_bed_duration,
    sleep_awake_duration,
    sleep_light_duration,
    sleep_rem_duration,
    sleep_deep_duration,
    sleep_regularity,
    sleep_latency,
    sleep_efficiency,
    heart_rate_resting,
    heart_rate_sleep,
    heart_rate_variability_sdnn,
    heart_rate_variability_rmssd,
    respiratory_rate,
    respiratory_rate_sleep,
    oxygen_saturation,
    oxygen_saturation_sleep,
    vo2_max,
    blood_glucose,
    blood_pressure_systolic,
    blood_pressure_diastolic,
    body_temperature_basal,
    skin_temperature_sleep,
}
```

</docgen-api>

---

Copyright © 2022 Sahha. All rights reserved.
