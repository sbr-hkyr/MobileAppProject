# TDM Insight

### Native Android Therapeutic Drug Monitoring Calculator

**CDE2313 – Mobile Application Development**
**Group 7**
**Albukhary International University**.
,
---

## 👥 Group Members
,
| Student Name       | Student ID  |
| ------------------ | ----------- |
| Munawa Abudujilili | AIU24102401 |
| Sebire Hakyar      | AIU24102378 |
| Shee Rashid Dina   | AIU24102392 |.

---

## 📖 Case Study

### Problem Overview

Hospital pharmacy departments perform **Therapeutic Drug Monitoring (TDM)** calculations for selected medicines. These calculations may require patient information, medication dosage, dosing intervals, drug concentrations, sampling times, and laboratory information.

The assigned case study required the development of **TDM Insight**, a native Android application that brings these inputs and pharmacokinetic calculations together in a structured and understandable mobile application.

The project focuses on **Vancomycin Therapeutic Drug Monitoring** and supports three main calculation workflows:

* **Vancomycin Pre** – using a pre-dose concentration.
* **Vancomycin Post** – using a post-dose concentration and sampling information.
* **Vancomycin Pre + Post** – using both pre-dose and post-dose concentrations with their corresponding timing information.

The case study also requires dynamic input forms, validation, a dedicated calculation engine, intermediate and final pharmacokinetic results, and explainable calculations.

### Implemented Solution

TDM Insight was implemented as a **native Android application using Kotlin and Jetpack Compose**.

The completed application provides:

1. Fictional patient and clinical case input.
2. Dynamic Vancomycin TDM workflows.
3. Input and cross-field validation.
4. A dedicated pharmacokinetic calculation engine.
5. Intermediate and final calculation results.
6. Step-by-step calculation explanations.
7. Local calculation history.
8. What-if dosing simulation.
9. Concentration-time visualization.
10. Laboratory-report camera and OCR functionality.
11. Review and confirmation of OCR-extracted values.
12. Light, dark, and system theme support.

All demonstration cases are fictional and the application is intended as an academic software prototype.

---

# ⭐ Key Implemented Features

## 1. Vancomycin TDM Workflows

TDM Insight supports three calculation pathways:

### Vancomycin Pre

Uses a pre-dose/trough concentration together with the required patient, dosing, and sampling information.

### Vancomycin Post

Uses a post-dose concentration and its corresponding sampling information.

### Vancomycin Pre + Post

Uses both pre-dose and post-dose concentrations with their corresponding timing information for two-point pharmacokinetic estimation.

The calculator dynamically changes the required input fields according to the selected workflow.

---

## 2. Patient and Medication Information

The calculator supports structured input for:

* Patient name
* Patient ID
* Age
* Gender
* Height
* Weight
* Serum creatinine
* Creatinine unit
* Vancomycin dose
* Dosing interval
* Infusion duration
* Pre-dose concentration
* Post-dose concentration
* Sampling times
* Infection indication
* MIC value

---

## 3. Pharmacokinetic Calculation Engine

The project contains a dedicated:

```text
TdmCalculationEngine
```

which separates pharmacokinetic calculations from the user interface.

Depending on the selected workflow, the application calculates parameters including:

* Creatinine clearance (CrCl)
* Ideal Body Weight (IBW)
* Adjusted Body Weight (AdjBW)
* Dosing weight
* Elimination rate constant (Ke)
* Elimination half-life
* Volume of distribution (Vd)
* Clearance
* Peak concentration (Cmax)
* Trough concentration (Cmin)
* AUC
* 24-hour AUC (AUC₂₄)
* AUC₂₄/MIC

The Pre + Post workflow implements two-point pharmacokinetic estimation.

The calculation architecture follows the case-study requirement:

```text
UI
 ↓
Input State
 ↓
Validation
 ↓
TDM Calculation Engine
 ↓
Result Model
 ↓
Results UI
```

The case study specifically requires the calculation engine to be separated from Composable/UI functions.

---

## 4. Input Validation

The application performs validation before calculations are executed.

Validation includes:

* Required-field validation
* Numeric validation
* Range validation
* Unit validation
* Cross-field validation
* Sampling-time validation
* Dose validation
* Dosing-interval validation
* Workflow-specific required-field validation
* Protection against invalid mathematical operations
* Warnings for values requiring review

This reflects the case-study requirement that validation should go beyond checking whether a field is empty.

---

## 5. Explainable Calculation Results

TDM Insight provides users with a step-by-step explanation of the calculation rather than displaying only the final result.

The calculation flow is presented as:

```text
Input Values
      ↓
Intermediate Calculations
      ↓
Pharmacokinetic Parameters
      ↓
Final Results
```

The explanation includes relevant formulas, input values, intermediate calculations, and resulting pharmacokinetic parameters.

This follows the case-study requirement for explainable results.

---

## 6. Therapeutic Assessment

The application evaluates Vancomycin exposure and categorizes AUC₂₄ results as:

* **Sub-therapeutic**
* **Therapeutic**
* **Supra-therapeutic / Toxic Risk**

Relevant concentration results are also assessed according to the configured indication and target values.

> These assessments are provided for academic demonstration only and must not be interpreted as real-world clinical prescribing advice.

---

## 7. What-If Regimen Simulation

The application includes a **What-If Regimen Simulation** feature.

Users can explore alternative:

* Vancomycin doses
* Dosing intervals

The simulator provides estimated results including:

* Cmax
* Cmin
* AUC₂₄
* Exposure status

A concentration-time curve is also displayed to visualize the simulated pharmacokinetic profile.

---

## 8. Calculation History

TDM Insight uses **Android Room** for local calculation history.

Users can save and review previous calculations directly on the device.

The application does not require:

* User accounts
* Cloud storage
* A backend server
* API keys

---

## 9. Laboratory Camera and OCR

TDM Insight includes a laboratory-report capture and OCR workflow using **Google ML Kit Text Recognition**.

The workflow is:

```text
Capture Laboratory Report
          ↓
       OCR Scan
          ↓
   Extract Information
          ↓
     Review Values
          ↓
    Confirm Values
          ↓
 Use in TDM Calculation
```

Extracted values require user review and confirmation before being used in the calculation workflow.

This follows the optional camera workflow described in the case study.

---

## 10. Concentration-Time Visualization

The application provides a concentration-time graph for pharmacokinetic visualization and regimen simulation.

This allows users to visually explore changes in Vancomycin concentration over time.

---

## 11. Theme Support

The application supports:

* Light mode
* Dark mode
* System default mode

The theme is managed through the application's state and integrated with Jetpack Compose.

---

# 🛠️ Technology Stack

| Technology             | Purpose                         |
| ---------------------- | ------------------------------- |
| **Kotlin**             | Main programming language       |
| **Android Studio**     | Android development environment |
| **Jetpack Compose**    | User interface                  |
| **Material 3**         | UI design system                |
| **ViewModel**          | Application state management    |
| **Room**               | Local calculation history       |
| **Google ML Kit**      | OCR/text recognition            |
| **Coil**               | Image loading                   |
| **Kotlin Coroutines**  | Asynchronous operations         |
| **JUnit**              | Unit testing                    |
| **Robolectric**        | JVM/Android testing             |
| **Compose UI Testing** | UI testing                      |
| **Roborazzi**          | Screenshot/UI testing           |

---

# 🏗️ Application Architecture

TDM Insight uses a layered architecture that separates the UI, application state, data persistence, and calculation logic.

```text
┌────────────────────────────┐
│      Jetpack Compose UI    │
│                            │
│ Calculator / Results /     │
│ History / Camera /         │
│ Simulator / Explanation   │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│       TdmViewModel         │
│                            │
│ Application State & Flow   │
└─────────────┬──────────────┘
              │
       ┌──────┴──────┐
       ▼             ▼
┌─────────────┐ ┌──────────────┐
│ Validation  │ │ Room Database│
└──────┬──────┘ └──────────────┘
       │
       ▼
┌────────────────────────────┐
│   TdmCalculationEngine     │
│                            │
│ Pharmacokinetic Calculations│
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│       Result Models        │
│                            │
│ Results / Explanation /    │
│ Simulation                 │
└────────────────────────────┘
```

### Main Components

| Component                     | Role                                            |
| ----------------------------- | ----------------------------------------------- |
| `MainActivity.kt`             | Application entry point and theme configuration |
| `TdmApp.kt`                   | Main application UI and navigation              |
| `TdmViewModel.kt`             | Application state and feature coordination      |
| `TdmCalculationEngine.kt`     | Pharmacokinetic calculations and validation     |
| `TdmInput.kt`                 | Structured TDM calculation input                |
| `PkResult.kt`                 | Pharmacokinetic result model                    |
| `Patient.kt`                  | Patient information model                       |
| `FictionalCases.kt`           | Fictional demonstration cases                   |
| `CalculationHistoryEntity.kt` | Room history data model                         |
| `CalculationHistoryDao.kt`    | Database access                                 |
| `TdmDatabase.kt`              | Room database configuration                     |
| `LabReportExtractor.kt`       | OCR/laboratory report extraction                |
| `PkCurveChart.kt`             | Concentration-time visualization                |

---

# 📂 GitHub Repository Structure

The repository contains the Android application together with project documentation, presentation material, screenshots, AI usage documentation, and the release APK.

```text
MobileAppProject-master/
│
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   │
│       │   └── java/
│       │       └── com/
│       │           └── example/
│       │               ├── MainActivity.kt
│       │               │
│       │               ├── tdm/
│       │               │   ├── camera/
│       │               │   │   └── LabReportExtractor.kt
│       │               │   │
│       │               │   ├── data/
│       │               │   │   ├── CalculationHistoryDao.kt
│       │               │   │   ├── CalculationHistoryEntity.kt
│       │               │   │   └── TdmDatabase.kt
│       │               │   │
│       │               │   ├── engine/
│       │               │   │   └── TdmCalculationEngine.kt
│       │               │   │
│       │               │   ├── model/
│       │               │   │   ├── FictionalCases.kt
│       │               │   │   ├── Patient.kt
│       │               │   │   ├── PkResult.kt
│       │               │   │   ├── TdmInput.kt
│       │               │   │   └── ThemeMode.kt
│       │               │   │
│       │               │   ├── ui/
│       │               │   │   ├── TdmApp.kt
│       │               │   │   ├── components/
│       │               │   │   │   └── PkCurveChart.kt
│       │               │   │   └── screens/
│       │               │   │       ├── CalculatorScreen.kt
│       │               │   │       ├── CameraScreen.kt
│       │               │   │       ├── DisclaimerScreen.kt
│       │               │   │       ├── ExplanationScreen.kt
│       │               │   │       ├── HistoryScreen.kt
│       │               │   │       ├── ResultsScreen.kt
│       │               │   │       └── SimulatorScreen.kt
│       │               │   │
│       │               │   └── viewmodel/
│       │               │       └── TdmViewModel.kt
│       │               │
│       │               └── ui/
│       │                   └── theme/
│       │
│       ├── test/
│       │   └── java/
│       │
│       └── androidTest/
│           └── java/
│
├── ai/
│   └── AI_Usage_Log.md
│
├── apk/
│   └── app-release.apk
│
├── assets/
│   └── README.md
│
├── docs/
│   ├── Case_Study_Analysis.md
│   ├── diagrams/
│   │   └── README.md
│   └── wireframe/
│       └── README.md
│
├── presentation/
│   └── README.md
│
├── screenshots/
│   └── README.md
│
├── Screenshots.pdf
├── LICENSE
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
├── gradlew
├── gradlew.bat
└── README.md
```

---

# 📥 Installation Guide

## Requirements

* Android Studio
* Android SDK Platform 36.1
* Android Build Tools 36.0.0
* Android device or emulator running **API 24 or higher**
* Java/JDK 11 compatible with the project configuration
* Internet connection for initial Gradle dependency downloads

## Using Android Studio

1. Clone the repository.

```bash
git clone <GITHUB_REPOSITORY_URL>
```

2. Open the project in Android Studio.

3. Allow Gradle synchronization to complete.

4. Connect an Android device or start an Android emulator.

5. Select the `app` configuration.

6. Click **Run**.

The application does not require an account, API key, backend server, or cloud service.

---

# 🔨 How to Build the Project

The project includes the Gradle wrapper, so Gradle does not need to be installed separately.

### Build Debug APK

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

### Run Unit Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

### Run Lint

```powershell
.\gradlew.bat lintDebug
```

### Run Android Instrumentation Tests

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The generated APK is produced under:

```text
app/build/outputs/apk/
```

---

# 📱 APK Download

A release APK is included directly in the repository:

**[Download TDM Insight APK](./apk/app-release.apk)**

The APK can also be installed directly on a compatible Android device.

> The included APK is intended for academic demonstration and testing.

---

# 📸 Screenshots

Screenshots documenting the completed application are included in the repository.

### Screenshot Collection

A complete screenshot document is available here:

**[View Screenshots PDF](./Screenshots.pdf)**

The repository also contains the `screenshots/` directory for screenshot-related project material.

The documented application screens include functionality such as:

* Calculator
* Vancomycin workflow selection
* Results
* Calculation explanation
* History
* Simulation
* Camera/OCR
* Application settings and theme

---

# 📚 Project Documentation

Additional project documentation is available in the repository:

### Case Study Analysis

```text
docs/Case_Study_Analysis.md
```

Contains the project's analysis of the assigned case study.

### Wireframes

```text
docs/wireframe/
```

Contains wireframe-related project documentation.

### Diagrams

```text
docs/diagrams/
```

Contains system/project diagrams.

### Presentation

```text
presentation/
```

Contains project presentation-related material.

### AI Usage Documentation

```text
ai/AI_Usage_Log.md
```

Documents how AI tools were used as development assistance during the project.

The AI usage log states that AI assistance was used for areas including debugging, refactoring, UI iteration, documentation drafting, and test planning, with generated suggestions reviewed, adapted, built, and tested by the students.

---

# 🧪 Testing

The project contains both unit tests and Android instrumentation tests.

Testing covers areas including:

* Vancomycin Pre calculations
* Vancomycin Post calculations
* Vancomycin Pre + Post calculations
* Calculation loading
* Input validation
* Pharmacokinetic result validity
* Cross-field validation
* What-if simulation
* OCR functionality
* Application startup
* Navigation
* Back navigation
* Calculator reset behaviour
* State restoration

Testing commands:

```powershell
.\gradlew.bat testDebugUnitTest
```

and:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Automated software tests verify implementation behaviour but do not establish clinical validity.

---

# ⚠️ Clinical Disclaimer

TDM Insight is an **academic software prototype** developed for CDE2313 – Mobile Application Development.

It is intended only for educational and software-development purposes.

The application must not be presented or used as:

* A clinically validated prescribing system
* A diagnostic system
* An autonomous treatment-decision system
* A replacement for qualified healthcare professionals
* A replacement for hospital protocols or clinical guidelines

All patient cases and laboratory information used for demonstrations are fictional.

The original case study explicitly states that TDM Insight must not be presented as a clinically validated prescribing, diagnostic, or autonomous treatment-decision system.

---

# 🙏 Acknowledgements

We would like to acknowledge:

* **Ts. Mohd Zulkifli Mohd Zaki** – Lead Instructor
* **Madam Siti Shafrah Shahawai** – Co-Lead Instructor
* **Albukhary International University**
* The **CDE2313 Mobile Application Development** course team for providing the case study and project requirements.
* The clinical and technical resources used to understand Therapeutic Drug Monitoring and Vancomycin pharmacokinetics.

---

# 📚 References

The assigned case study identifies the following resources as starting points for TDM domain understanding and reference checking:

1. **myTDM Calculator**
   https://www.mytdmcalculator.com/

2. **Malaysian Pharmacy Information System (PhIS) TDM Calculator documentation**

3. **Current authoritative Vancomycin Therapeutic Drug Monitoring guidance**

The exact clinical equations, assumptions, target values, and reference ranges should be interpreted together with their corresponding authoritative clinical sources.

---

# 📌 Project Information

| Item               | Details                                  |
| ------------------ | ---------------------------------------- |
| **Project**        | TDM Insight                              |
| **Course**         | CDE2313 – Mobile Application Development |
| **Group**          | Group 7                                  |
| **Institution**    | Albukhary International University       |
| **Platform**       | Native Android                           |
| **Language**       | Kotlin                                   |
| **UI Framework**   | Jetpack Compose                          |
| **Design System**  | Material 3                               |
| **Application ID** | `com.tdminsight.app`                     |
| **Minimum SDK**    | API 24                                   |
| **Target SDK**     | API 36                                   |
| **Compile SDK**    | Android 36.1                             |
| **Version**        | 1.0                                      |

---

## 👥 Group 7

**Munawa Abudujilili** — `AIU24102401`
**Sebire Hakyar** — `AIU24102378`
**Shee Rashid Dina** — `AIU24102392`
