# TDM Insight

Native Kotlin / Jetpack Compose application for CDE2313.

## Run in Android Studio

Open this folder, sync Gradle, select the app configuration and an API 24+ device, then Run.
Requirements: Android Studio bundled JDK compatible with the included Gradle wrapper, SDK Platform 36.1, Build Tools 36.0.0. Initial dependency downloads need Internet access. Android Studio manages local.properties for this computer.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

Application ID: `com.tdminsight.app`. This installs separately from earlier application identities; their local history is not migrated automatically. Debug signing uses the local development key. Use Android Studio's signed APK wizard with your own key for release distribution.

## Architecture

- Kotlin calculation engine and structured input/result models.
- Compose screens and ViewModel state.
- Local Room calculation history.
- Bundled ML Kit OCR and native Android camera/photo picker.

No account, API key, backend or web hosting configuration is required. ML Kit retains its normal transitive component libraries and network permissions.

## Verification

Test fictional Pre, Post and Pre + Post cases, validation, explanations, history, simulation, camera permission handling and OCR confirmation on your device. Clinical equations are not clinically validated by software build tests.

For CDE2313, document development assistance honestly in the AI usage log, understand the implementation and record your own device tests. The application remains an academic prototype.

