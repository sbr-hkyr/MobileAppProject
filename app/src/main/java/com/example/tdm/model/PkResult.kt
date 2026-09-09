package com.example.tdm.model

data class CalculationStep(
  val stepNumber: Int,
  val category: StepCategory,
  val title: String,
  val formula: String,
  val substitution: String,
  val result: String,
  val notes: String = ""
)

enum class StepCategory(val display: String) {
  INPUT_BASELINE("1. Input & Baseline"),
  INTERMEDIATE("2. Intermediate Calculations"),
  PHARMACOKINETICS("3. Pharmacokinetic Parameters"),
  FINAL_OUTCOMES("4. Final Result & Regimen Evaluation")
}

enum class TherapeuticStatus(val label: String, val colorHex: Long) {
  SUBTHERAPEUTIC("Sub-therapeutic", 0xFFE65100),
  THERAPEUTIC("Therapeutic", 0xFF00897B),
  SUPRATHERAPEUTIC("Supra-therapeutic / Toxic Risk", 0xFFC62828)
}

data class PkResult(
  val workflow: TdmWorkflow,
  // Baseline renal & anthropometric
  val ibwKg: Double,
  val isObese: Boolean,
  val adjBwKg: Double?,
  val dosingWeightKg: Double,
  val crClMlPerMin: Double,
  val serumCreatinineUmolL: Double,

  // Pharmacokinetic parameters
  val kePerHour: Double,
  val halfLifeHours: Double,
  val vdLiters: Double,
  val vdPerKg: Double,
  val clearanceLPerHr: Double,
  val clearanceMlPerMin: Double,

  // Concentration outcomes
  val cMaxMgL: Double,
  val cMinMgL: Double,
  val measuredPostSampleConc: Double?,
  val measuredPreSampleConc: Double?,

  // AUC metrics
  val aucTauMgHPerL: Double,
  val auc24MgHPerL: Double,
  val micMgL: Double,
  val auc24OverMic: Double,

  // Status & Guidance
  val aucStatus: TherapeuticStatus,
  val troughStatus: TherapeuticStatus,
  val clinicalRecommendation: String,
  val detailedSteps: List<CalculationStep>
)
