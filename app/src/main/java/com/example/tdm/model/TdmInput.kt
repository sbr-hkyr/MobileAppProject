package com.example.tdm.model

data class ValidationError(
  val field: String,
  val message: String
)

data class ValidationWarning(
  val field: String,
  val message: String
)

data class ValidationResult(
  val isValid: Boolean,
  val errors: List<ValidationError> = emptyList(),
  val warnings: List<ValidationWarning> = emptyList()
)

data class TdmInput(
  val workflow: TdmWorkflow = TdmWorkflow.PRE_POST,
  val patient: Patient = Patient(),
  val regimen: DosingRegimen = DosingRegimen(),
  val targetIndication: TargetIndication = TargetIndication.SEVERE_MRSA,
  val micMgL: Double = 1.0,

  // Lab samples - dynamic based on workflow
  val preDoseConcMgL: Double? = 11.5,
  val preDoseSampleDelayHoursBeforeNextDose: Double? = 0.5,

  val postDoseConcMgL: Double? = 27.8,
  val postDoseSampleDelayHoursAfterInfusionEnd: Double? = 1.0
)
