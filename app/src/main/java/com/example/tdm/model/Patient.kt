package com.example.tdm.model

enum class Gender(val label: String) {
  MALE("Male"),
  FEMALE("Female")
}

enum class CreatinineUnit(val label: String, val symbol: String) {
  UMOL_L("Micromoles per liter", "µmol/L"),
  MG_DL("Milligrams per deciliter", "mg/dL")
}

enum class TargetIndication(val label: String, val targetTroughRange: String) {
  MILD_MODERATE("Uncomplicated / Non-severe (Target Trough: 10–15 mg/L)", "10–15 mg/L"),
  SEVERE_MRSA("Severe / Invasive MRSA (Target Trough: 15–20 mg/L)", "15–20 mg/L")
}

enum class TdmWorkflow(val title: String, val shortLabel: String, val description: String) {
  PRE("Vancomycin Pre", "Pre (Trough)", "Calculation based on a pre-dose (trough) concentration."),
  POST("Vancomycin Post", "Post (Peak)", "Calculation based on a post-dose (peak) concentration and sampling delay."),
  PRE_POST("Vancomycin Pre + Post", "Pre + Post (Two-Point)", "Sawchuk-Zaske two-point pharmacokinetic estimation using both peak and trough samples.")
}

data class Patient(
  val patientId: String = "P-10492",
  val patientName: String = "Fictional Patient",
  val ageYears: Double = 52.0,
  val gender: Gender = Gender.MALE,
  val heightCm: Double = 172.0,
  val weightKg: Double = 70.0,
  val serumCreatinine: Double = 85.0,
  val creatinineUnit: CreatinineUnit = CreatinineUnit.UMOL_L
)

data class DosingRegimen(
  val doseMg: Double = 1000.0,
  val intervalHours: Double = 12.0,
  val infusionDurationHours: Double = 1.0
)
