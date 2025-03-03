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


In the `AndroidManifest.xml` file, declare HealthConnect data types if required
```xml
<!-- Sleep -->
<uses-permission android:name="android.permission.health.READ_SLEEP" />

<!-- Activity -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_FLOORS_CLIMBED" />
```


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
configure(
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
Sahha.configure(this, settings) { error, success ->
  error?.also { e -> Log.e(TAG, e) }
  Log.d(TAG, success)
}
```

---

### isAuthenticated()

```kotlin
isAuthenticated(): Boolean
```

**Example usage**:
```kotlin
val isAuthenticated = Sahha.isAuthenticated()

if (isAuthenticated == false) {
  // E.g. Authenticate the user
}
```

---

### authenticate(...)

```kotlin
authenticate(
  appId: String,
  appSecret: String,
  externalId: String,
  callback: (error: String?, success: boolean) -> Unit
)
```

**Example usage**:
```kotlin
Sahha.authenticate(
  APP_ID,
  APP_SECRET,
  EXTERNAL_ID, // Some unique identifier for the user
) { error, success ->
  if (success) {
    // E.g. Continue onboarding flow
  } else {
    error?.also { e -> Log.e(TAG, e) }
  }
}
```

---

Copyright © 2022 Sahha. All rights reserved.
