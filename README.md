# TDM Insight

**TDM Insight** is a native Android Therapeutic Drug Monitoring (TDM) calculator developed for **CDE2313 – Mobile Application Development** at Albukhary International University.

The application focuses on **Vancomycin pharmacokinetics** and provides three laboratory-data workflows, automated pharmacokinetic calculations, input validation, explainable calculation steps, regimen simulation, calculation history, and an optional laboratory-report OCR workflow.

> **Academic Prototype:** TDM Insight is intended strictly for educational and software-development purposes. It is not a clinically validated prescribing, diagnostic, or autonomous treatment-decision system. All demonstration patient cases are fictional.

---

## Repository structure

```text
TDM-Insight/
├── README.md                 # Project landing page
├── LICENSE
├── .gitignore
├── app/                      # Android Studio application module
├── gradle/                   # Gradle configuration and wrapper
├── screenshots/              # Demonstration screenshots
├── docs/                     # Case study, wireframes, and diagrams
├── apk/app-release.apk       # Debug-build APK for demonstration
├── presentation/             # Final presentation files
├── ai/AI_Usage_Log.md        # AI assistance declaration
└── assets/                   # Supporting resources
```

The APK in `apk/` is provided for demonstration. The project remains the source of truth for building the application.

---

## Features

### 🧮 Vancomycin TDM Calculator

TDM Insight supports three calculation workflows:

* **Vancomycin Pre** – calculation using a pre-dose/trough concentration.
* **Vancomycin Post** – calculation using a post-dose/peak concentration and sampling delay.
* **Vancomycin Pre + Post** – two-point pharmacokinetic estimation using both pre-dose and post-dose concentrations.

The selected workflow dynamically determines which laboratory inputs are required.

---

### 📋 Patient & Dosing Inputs

The calculator accepts structured patient and medication information including:

* Patient name and ID
* Age
* Gender
* Height
* Weight
* Serum creatinine
* Creatinine unit (`µmol/L` or `mg/dL`)
* Vancomycin dose
* Dosing interval
* Infusion duration
* Pre-dose concentration and sampling time
* Post-dose concentration and sampling time
* Target infection indication
* MIC value

---

### ✅ Input Validation

The calculation engine performs validation before calculations are executed.

Validation includes:

* Required-field validation
* Numeric and range validation
* Serum creatinine validation
* Dose and dosing-interval validation
* Infusion-duration validation
* Sampling-time validation
* Peak/trough relationship validation
* Cross-field timing validation
* Infusion-rate warnings
* Detection of unusually high laboratory concentrations
* Protection against invalid calculation conditions

The application distinguishes between **calculation-blocking errors** and **clinical review warnings**.

---

## Pharmacokinetic Calculations

The calculation engine produces intermediate and final pharmacokinetic parameters.

Depending on the selected workflow, the application calculates:

* Serum creatinine standardization
* Ideal Body Weight (IBW)
* Adjusted Body Weight (AdjBW), where applicable
* Dosing weight
* Creatinine clearance (CrCl)
* Elimination rate constant (Ke)
* Elimination half-life
* Volume of distribution (Vd)
* Clearance
* Peak concentration (Cmax)
* Trough concentration (Cmin)
* AUC over the dosing interval
* 24-hour AUC (AUC₂₄)
* AUC₂₄/MIC

The calculation engine uses workflow-specific pharmacokinetic pathways, including the two-point **Sawchuk-Zaske** approach for the Pre + Post workflow.

For the Pre and Post workflows, elimination rate is estimated from renal function before the remaining pharmacokinetic parameters are calculated.

---

## Explainable Calculations

TDM Insight does not display only a final numerical result.

Each calculation produces a sequence of explainable steps containing:

1. Calculation category
2. Formula
3. Substituted patient values
4. Calculated result
5. Supporting notes

The application organizes the explanation into four stages:

```text
Input & Baseline
        ↓
Intermediate Calculations
        ↓
Pharmacokinetic Parameters
        ↓
Final Result & Regimen Evaluation
```

This allows users to follow how the pharmacokinetic results were derived.

---

## Clinical Assessment

The results screen evaluates calculated exposure against configured therapeutic ranges.

AUC₂₄ is categorized as:

* **Sub-therapeutic:** below 400 mg·h/L
* **Therapeutic:** 400–600 mg·h/L
* **Supra-therapeutic / Toxic Risk:** above 600 mg·h/L

The application also evaluates the calculated trough concentration against the selected target indication.

The results screen provides a clinical assessment and regimen recommendation based on the calculated exposure.

> These recommendations are part of an academic software prototype and must not be used as real-world clinical prescribing instructions.

---

## What-If Regimen Simulation

The **Simulation** feature allows users to explore alternative Vancomycin dosing regimens using the patient's calculated pharmacokinetic parameters.

Users can modify:

* Proposed dose
* Dosing interval

The simulator estimates:

* Cmax
* Cmin
* AUC₂₄
* Exposure status

The application also provides a concentration-time curve to visualize the simulated pharmacokinetic profile.

This allows users to compare the current regimen with a proposed alternative without replacing the original calculation.

---

## Laboratory Camera & OCR

TDM Insight includes an optional laboratory-report scanning workflow.

```text
Capture Lab Report
        ↓
On-device OCR
        ↓
Extract Values
        ↓
Review Extracted Data
        ↓
Confirm Values
        ↓
Use in TDM Calculation
```

The application uses **Google ML Kit Text Recognition** to extract relevant information from a laboratory report, including:

* Patient name
* Patient ID
* Serum creatinine
* Pre-dose concentration
* Post-dose concentration
* Vancomycin dose
* Dosing interval

Extracted values are **not automatically committed** to the calculation. Users must review and confirm the extracted information first.

A captured laboratory report can also be attached to the corresponding calculation history record.

The project includes simulated fictional laboratory reports for demonstration and testing.

---

## Calculation History

TDM Insight provides local calculation history using **Android Room**.

Saved records can contain:

* Patient information
* Dosing regimen
* Pharmacokinetic results
* AUC₂₄
* Trough information
* Attached laboratory-report information where applicable

History is stored locally on the device and does not require a cloud backend.

---

## Fictional Demonstration Cases

The project includes three fictional cases for demonstrating the supported workflows:

### Case 1 — Standard Adult Sepsis

**Workflow:** Pre + Post

A fictional adult patient with MRSA bacteremia receiving Vancomycin 1000 mg every 12 hours, with both peak and trough concentrations available.

### Case 2 — Geriatric Renal Impairment

**Workflow:** Pre

A fictional older patient with reduced renal clearance receiving Vancomycin 750 mg every 24 hours with a trough concentration available.

### Case 3 — ICU / High Clearance

**Workflow:** Post

A fictional younger patient with high renal clearance receiving Vancomycin 1500 mg every 12 hours with a post-dose concentration available.

All patient identities and clinical cases included in the application are fictional and intended for academic demonstration only.

---

## Architecture

The application separates the user interface from the pharmacokinetic calculation logic.

```text
Jetpack Compose UI
        ↓
TdmViewModel
        ↓
Input State & Validation
        ↓
TdmCalculationEngine
        ↓
PkResult / SimulationResult
        ↓
Results & Explanation UI
```

### Main architectural components

| Component              | Purpose                                                 |
| ---------------------- | ------------------------------------------------------- |
| `MainActivity`         | Android entry point and theme configuration             |
| `TdmApp`               | Application navigation and main Compose UI              |
| `TdmViewModel`         | Application state, navigation and feature coordination  |
| `TdmCalculationEngine` | Validation, pharmacokinetic calculations and simulation |
| `TdmInput`             | Structured calculation input model                      |
| `PkResult`             | Structured pharmacokinetic result model                 |
| `CalculationStep`      | Explainable calculation information                     |
| Room Database          | Local calculation history                               |
| `LabReportExtractor`   | ML Kit OCR and laboratory-data extraction               |
| `PkCurveChart`         | Pharmacokinetic concentration-time visualization        |

---

## Project Structure

```text
app/
└── src/
    ├── main/
    │   ├── java/com/example/
    │   │   ├── MainActivity.kt
    │   │   │
    │   │   ├── tdm/
    │   │   │   ├── camera/
    │   │   │   │   └── LabReportExtractor.kt
    │   │   │   │
    │   │   │   ├── data/
    │   │   │   │   ├── CalculationHistoryDao.kt
    │   │   │   │   ├── CalculationHistoryEntity.kt
    │   │   │   │   └── TdmDatabase.kt
    │   │   │   │
    │   │   │   ├── engine/
    │   │   │   │   └── TdmCalculationEngine.kt
    │   │   │   │
    │   │   │   ├── model/
    │   │   │   │   ├── FictionalCases.kt
    │   │   │   │   ├── Patient.kt
    │   │   │   │   ├── PkResult.kt
    │   │   │   │   ├── TdmInput.kt
    │   │   │   │   └── ThemeMode.kt
    │   │   │   │
    │   │   │   ├── ui/
    │   │   │   │   ├── TdmApp.kt
    │   │   │   │   ├── components/
    │   │   │   │   │   └── PkCurveChart.kt
    │   │   │   │   └── screens/
    │   │   │   │       ├── CalculatorScreen.kt
    │   │   │   │       ├── CameraScreen.kt
    │   │   │   │       ├── DisclaimerScreen.kt
    │   │   │   │       ├── ExplanationScreen.kt
    │   │   │   │       ├── HistoryScreen.kt
    │   │   │   │       ├── ResultsScreen.kt
    │   │   │   │       └── SimulatorScreen.kt
    │   │   │   │
    │   │   │   └── viewmodel/
    │   │   │       └── TdmViewModel.kt
    │   │   │
    │   │   └── AndroidManifest.xml
    │   │
    │   └── ...
    └── test/
        └── ...
```

---

## Technology Stack

* **Language:** Kotlin
* **Platform:** Native Android
* **UI:** Jetpack Compose
* **Design System:** Material 3
* **Architecture:** MVVM with a dedicated calculation engine
* **Local Database:** Room
* **OCR:** Google ML Kit Text Recognition
* **Image Loading:** Coil
* **Asynchronous Processing:** Kotlin Coroutines
* **Testing:** JUnit, Robolectric, AndroidX Compose UI Testing
* **UI Screenshot Testing:** Roborazzi

---

## Requirements

* Android Studio
* Android SDK Platform 36.1
* Android Build Tools 36.0.0
* Android device or emulator with **API 24 or higher**
* Compatible JDK supplied/configured with Android Studio
* Internet connection for initial Gradle dependency downloads

No account, API key, cloud backend, or web-hosting configuration is required.

---

## Running the Project

### Android Studio

1. Clone or download the repository.
2. Open the project in Android Studio.
3. Allow Gradle synchronization to complete.
4. Select an Android API 24+ device or emulator.
5. Select the `app` configuration.
6. Click **Run**.

### Gradle

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests and lint:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

Run connected Android tests:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

---

## Application Configuration

**Application ID**

```text
com.tdminsight.app
```

**Minimum SDK:** API 24

**Target SDK:** API 36

**Compile SDK:** 36.1

The debug build uses the local development signing configuration. For release distribution, use Android Studio's signed APK/App Bundle workflow with an appropriate release key.

---

## Testing

The project contains automated unit, Robolectric, and Android instrumentation tests.

Testing covers areas including:

* Pre + Post calculation workflow
* Pre-only workflow
* Post-only workflow
* Pharmacokinetic result validity
* Cross-field concentration validation
* What-if regimen simulation
* Laboratory-report text extraction
* Application startup
* Navigation
* Back navigation
* State restoration
* Calculator reset behavior

Example test commands:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Automated tests verify software behaviour and calculation implementation; they do **not** establish clinical validity.

---

## Clinical Disclaimer

TDM Insight is an **academic software prototype** created for the CDE2313 Mobile Application Development course.

It must not be used as:

* A clinical prescribing system
* A diagnostic system
* An autonomous treatment-decision system
* A replacement for qualified healthcare professionals
* A substitute for current hospital protocols or clinical guidelines

All demonstration patients and laboratory records are fictional.

Clinical equations, reference values, and therapeutic targets should be checked against current authoritative clinical guidance before any real-world clinical use.

---

## References

The project case study identifies the following resources as starting points for therapeutic drug monitoring domain understanding and reference checking:

* **myTDM Calculator**
* **Malaysian Pharmacy Information System (PhIS) TDM Calculator documentation**
* Current authoritative Vancomycin TDM clinical guidance

The application should be understood as an academic implementation of the specified TDM workflow rather than a clinically validated calculator.

---

## Academic Context

**Course:** CDE2313 – Mobile Application Development
**Institution:** Albukhary International University
**Platform:** Native Android
**Application:** TDM Insight
**Version:** 1.0

The application was developed as an academic project demonstrating the integration of mobile UI development, structured data handling, pharmacokinetic calculations, validation, local persistence, OCR, visualization, and explainable results.
