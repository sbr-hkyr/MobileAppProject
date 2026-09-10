# TDM Insight

### Native Android Therapeutic Drug Monitoring Calculator

**CDE2313 – Mobile Application Development**
**Group 7**
**Albukhary International University**

---

## 👥 Group Members

| Student Name       | Student ID  |
| ------------------ | ----------- |
| Munawa Abudujilili | AIU24102401 |
| Sebire Hakyar      | AIU24102378 |
| Shee Rashid Dina   | AIU24102392 |

---

## 📖 Case Study

### Problem Overview

Hospital pharmacy departments perform **Therapeutic Drug Monitoring (TDM)** calculations for selected medicines. These calculations may require patient information, medication dosage, dosing intervals, drug concentrations, sampling times, and laboratory information.

The assigned case study required the development of **TDM Insight**, a native Android application that brings these inputs and pharmacokinetic calculations together in a structured and understandable mobile application.

The project focuses specifically on **Vancomycin Therapeutic Drug Monitoring** and provides different calculation workflows depending on the available laboratory measurements.

The case study requires the application to provide three main Vancomycin workflows:

* **Vancomycin Pre** – using a pre-dose concentration.
* **Vancomycin Post** – using a post-dose concentration and sampling information.
* **Vancomycin Pre + Post** – using both pre-dose and post-dose concentrations with their corresponding timing information.

The original case study also requires dynamic input forms, input validation, a dedicated calculation engine, intermediate and final pharmacokinetic results, and explanations of the calculations.

### Implemented Solution

TDM Insight was developed as a native Android application using **Kotlin and Jetpack Compose**.

The application provides a structured workflow in which users can:

1. Create or select a fictional patient case.
2. Enter patient and medication information.
3. Select Vancomycin as the medication.
4. Select the required TDM workflow.
5. Enter the laboratory and dosing information required by that workflow.
6. Validate the entered information.
7. Perform the pharmacokinetic calculation.
8. Review intermediate and final results.
9. View a step-by-step calculation explanation.
10. Save and review previous calculations.
11. Perform what-if dosing simulations.
12. Use the camera and OCR functionality to assist with laboratory-report data entry.

The application was designed as an **academic prototype** and all demonstration cases are fictional.

---

# ⭐ Key Implemented Features

## 1. Vancomycin TDM Workflows

The application supports:

* Vancomycin Pre
* Vancomycin Post
* Vancomycin Pre + Post

The input form dynamically adapts according to the selected workflow rather than displaying every possible field at once.

---

## 2. Patient and Medication Inputs

The application supports structured entry of information including:

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

The project contains a dedicated **TDM Calculation Engine** that separates calculation logic from the user interface.

The application calculates pharmacokinetic parameters including:

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

The Pre + Post workflow uses two-point pharmacokinetic estimation based on the implemented Sawchuk-Zaske approach.

The case study specifically requires that calculation logic be separated from Composable/UI functions and that structured input and result models be used.

---

## 4. Input Validation

The application validates user input before performing calculations.

Validation includes:

* Required-field validation
* Numeric validation
* Range validation
* Unit validation
* Cross-field validation
* Sampling-time validation
* Dose and dosing-interval validation
* Protection against invalid mathematical operations
* Detection of missing workflow-specific information
* Warnings for values requiring review

This goes beyond simply checking whether a field has been left empty, as required by the case study.

---

## 5. Explainable Results

TDM Insight provides more than a final numerical answer.

Users can view the calculation process through:

```text
Input Values
     ↓
Intermediate Calculations
     ↓
Pharmacokinetic Parameters
     ↓
Final Result
```

The explanation displays important inputs, formulas, intermediate values, and calculated results in a simplified sequence.

This follows the case-study requirement that users should be able to understand how the main results were obtained.

---

## 6. Therapeutic Assessment

The application evaluates calculated Vancomycin exposure and provides an interpretation of the calculated results.

AUC₂₄ is categorized into:

* **Sub-therapeutic**
* **Therapeutic**
* **Supra-therapeutic / Toxic Risk**

The application also evaluates relevant concentration results according to the selected indication and configured targets.

> These assessments are implemented for academic demonstration and must not be treated as clinical prescribing advice.

---

## 7. What-If Regimen Simulation

The application includes a **What-If Regimen Simulation** feature.

Users can explore alternative:

* Vancomycin doses
* Dosing intervals

The simulation provides estimated pharmacokinetic results such as:

* Cmax
* Cmin
* AUC₂₄
* Exposure status

A concentration-time curve is also provided to visualize the simulated regimen.

---

## 8. Calculation History

TDM Insight includes local calculation history using **Android Room**.

Users can save and review previous TDM calculations without requiring an online account or cloud backend.

The stored information can include patient, dosing, pharmacokinetic, and calculation-result information.

---

## 9. Camera and OCR

The application includes a laboratory-report camera workflow using **Google ML Kit Text Recognition**.

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

The OCR functionality assists with extracting relevant laboratory and medication information from fictional laboratory reports.

Extracted information is reviewed and confirmed by the user before being used in a calculation.

This follows the case-study concept of:

**Camera → Capture → Review → Confirm Value → Use in Calculation**.

---

## 10. Concentration-Time Visualization

The application provides a concentration-time graph for pharmacokinetic visualization and simulation.

This helps users understand how Vancomycin concentration changes over time under a selected dosing regimen.

---

## 11. Theme Support

The application supports:

* Light mode
* Dark mode
* System default mode

The theme is managed through the application's state and integrated with the Jetpack Compose UI.

---

# 🛠️ Technology Stack

| Technology             | Purpose                          |
| ---------------------- | -------------------------------- |
| **Kotlin**             | Application programming language |
| **Android Studio**     | Development environment          |
| **Jetpack Compose**    | User interface development       |
| **Material 3**         | UI design system                 |
| **Android ViewModel**  | Application state management     |
| **Room**               | Local calculation history        |
| **Google ML Kit**      | OCR/text recognition             |
| **Coil**               | Image loading                    |
| **Kotlin Coroutines**  | Asynchronous operations          |
| **JUnit**              | Unit testing                     |
| **Robolectric**        | Android/JVM testing              |
| **Compose UI Testing** | UI testing                       |
| **Roborazzi**          | UI screenshot testing            |

---

# 🏗️ Application Architecture

TDM Insight follows a structured architecture that separates the user interface from the calculation logic.

```text
┌─────────────────────────┐
│    Jetpack Compose UI   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│      TdmViewModel       │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ Input State & Validation │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ TdmCalculationEngine    │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   Pharmacokinetic       │
│       Results           │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ Results & Explanation   │
└─────────────────────────┘
```

### Main Components

* **`MainActivity`** – application entry point and theme configuration.
* **`TdmApp`** – main application UI and navigation.
* **`TdmViewModel`** – manages application state and coordinates application features.
* **`TdmCalculationEngine`** – performs validation and pharmacokinetic calculations.
* **Model classes** – represent structured TDM inputs, patients, results, calculation steps, and simulation data.
* **Room database** – stores local calculation history.
* **Lab Report Extractor** – handles OCR-based laboratory information extraction.
* **UI components** – provide calculation forms, results, explanations, history, simulation, camera functionality, and visualization.

---

# 📂 GitHub Repository Structure

```text
TDM-Insight/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/
│       │   │       ├── MainActivity.kt
│       │   │       │
│       │   │       └── tdm/
│       │   │           ├── camera/
│       │   │           ├── data/
│       │   │           ├── engine/
│       │   │           ├── model/
│       │   │           ├── ui/
│       │   │           └── viewmodel/
│       │   │
│       │   └── AndroidManifest.xml
│       │
│       └── test/
│
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

---

# 📥 Installation Guide

### Requirements

* Android Studio
* Android SDK Platform 36.1
* Android Build Tools 36.0.0
* Android device or emulator running **API 24 or higher**
* Compatible JDK configured through Android Studio
* Internet connection for the initial Gradle dependency download

### Installation Steps

1. Clone the repository:

```bash
git clone <GITHUB_REPOSITORY_URL>
```

2. Open the project in **Android Studio**.

3. Allow Android Studio to synchronize the Gradle files.

4. Connect an Android device or start an Android emulator.

5. Ensure the device uses **API 24 or higher**.

6. Select the `app` run configuration.

7. Click **Run**.

No user account, API key, backend, or cloud service is required.

---

# 🔨 How to Build the Project

### Windows

Build the debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Run lint:

```powershell
.\gradlew.bat lintDebug
```

Run Android instrumentation tests:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The generated debug APK can be found under the project's:

```text
app/build/outputs/apk/
```

---

# 📱 APK Download

The latest APK should be made available through the GitHub repository's **Releases** section or uploaded to the repository as required for project submission.

**APK:** `[Download TDM Insight APK](ADD_APK_LINK_HERE)`

> Replace `ADD_APK_LINK_HERE` with the actual GitHub APK/release link before submission.

---

# 📸 Screenshots

Screenshots of the completed application should be included here to demonstrate the main functionality.

### Main Screen

`[Insert screenshot here]`

### Vancomycin Pre Workflow

`[Insert screenshot here]`

### Vancomycin Post Workflow

`[Insert screenshot here]`

### Vancomycin Pre + Post Workflow

`[Insert screenshot here]`

### Calculation Results

`[Insert screenshot here]`

### Calculation Explanation

`[Insert screenshot here]`

### Calculation History

`[Insert screenshot here]`

### What-If Simulation

`[Insert screenshot here]`

### Camera / OCR

`[Insert screenshot here]`

### Dark Mode

`[Insert screenshot here]`

> Screenshots can be added to the repository under a `screenshots/` folder and referenced using Markdown image links.

---

# 🧪 Testing

The project includes automated tests covering key application and calculation functionality.

Testing includes:

* Vancomycin Pre calculations
* Vancomycin Post calculations
* Vancomycin Pre + Post calculations
* Input validation
* Cross-field validation
* Pharmacokinetic result validation
* What-if simulation
* OCR extraction
* Application startup
* Navigation
* Back navigation
* Calculator reset behaviour
* State restoration

Example:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Automated tests verify software behaviour but do not establish clinical validity.

---

# ⚠️ Clinical Disclaimer

TDM Insight is an **academic software prototype** developed for CDE2313 – Mobile Application Development.

It is intended only for educational and software-development purposes.

The application must **not** be presented or used as:

* A clinically validated prescribing system
* A diagnostic system
* An autonomous treatment-decision system
* A replacement for qualified healthcare professionals
* A replacement for hospital protocols or clinical guidelines

All patient cases and laboratory information used for demonstrations are fictional.

## The case study explicitly states that clinical equations, units, assumptions, and reference values should be supported by authoritative sources and that the application must not be presented as a clinically validated treatment-decision system.

# 🙏 Acknowledgements

We would like to acknowledge:

* **Ts. Mohd Zulkifli Mohd Zaki** – Lead Instructor
* **Madam Siti Shafrah Shahawai** – Co-Lead Instructor
* **Albukhary International University**
* The **CDE2313 Mobile Application Development** course team for providing the case study and project requirements.
* The clinical and technical references used to understand Therapeutic Drug Monitoring and Vancomycin pharmacokinetics.

---

# 📚 References

The following resources were identified in the project case study as references for domain understanding and clinical reference checking:

1. **myTDM Calculator**
   https://www.mytdmcalculator.com/

2. **Malaysian Pharmacy Information System (PhIS) TDM Calculator documentation**

3. **Current authoritative Vancomycin Therapeutic Drug Monitoring guidance**

The exact clinical equations, assumptions, target ranges, and reference values used in the application should be interpreted together with their corresponding authoritative clinical sources.

---

# 📌 Project Information

**Project:** TDM Insight
**Course:** CDE2313 – Mobile Application Development
**Group:** 7
**Institution:** Albukhary International University
**Platform:** Native Android
**Language:** Kotlin
**UI Framework:** Jetpack Compose
**Design System:** Material 3
**Application ID:** `com.tdminsight.app`

---

## 👥 Group 7

**Munawa Abudujilili** — `AIU24102401`
**Sebire Hakyar** — `AIU24102378`
**Shee Rashid Dina** — `AIU24102392`
