# Sahha SDK for Android Apps

The Sahha SDK provides a convenient way for Android apps to connect to the Sahha API.

---

## Docs

The Sahha Docs provide detailed instructions for installation and usage of the Sahha SDK.

[Sahha Docs](https://docs.sahha.ai)

---

## Example

The Sahha Demo App provides a convenient way to try the features of the Sahha SDK.

[Sahha Demo App](https://github.com/sahha-ai/sahha-demo-android)

---

## Install

In the **app** level `build.gradle` file
```groovy
dependencies {
  implementation 'ai.sahha:sahha-android:1.1.4'
}
```


In the `AndroidManifest.xml` file, declare Google Health Connect data types if required, e.g. Sleep and step count. More data types are available such as heart rate, workout / exercise, please refer to the links below for more information.
```xml
<!-- Sleep -->
<uses-permission android:name="android.permission.health.READ_SLEEP" />

<!-- Activity -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_FLOORS_CLIMBED" />
```

This is recommended if you'd like to retrieve data from other health apps such as WHOOP, Garmin, Samsung Health etc.

To declare other sensor permissions, please refer to [this](https://docs.sahha.ai/docs/data-flow/sdk/setup#step-2-review-sensor-permissions) page.

Only include the sensor permissions required by your project, what is declared here will be reviewed by the Play Store.

You must be able to justify reasons behind requiring the sensor permissions, [these](https://docs.sahha.ai/docs/data-flow/sdk/app-store-submission/google-play-store#data-type-justifications) justifications may be used to clearly articulate the reasoning behind your required sensor permissions.

---

## API

<docgen-index>

* [`configure(...)`](#configure)
* [`isAuthenticated()`](#isauthenticated)
* [`authenticate()`](#authenticate)

</docgen-index>

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
    callback: ((error: String?, demographic: SahhaDemographic?) -> Unit?)
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
    } else { // enabled, disabled (cannot be pending after enableSensors is called)
        // E.g. Continue flow (disabled can mean some permissions were granted and some were not (partially granted))
    }
}
```

---

Copyright © 2022 Sahha. All rights reserved.
