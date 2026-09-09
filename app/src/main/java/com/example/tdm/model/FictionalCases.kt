package com.example.tdm.model

data class FictionalCase(
  val id: String,
  val title: String,
  val tag: String,
  val clinicalSummary: String,
  val input: TdmInput
)

object FictionalCasesRepository {
  val sampleCases = listOf(
    FictionalCase(
      id = "case_1",
      title = "Case 1: Standard Adult Sepsis (Pre + Post)",
      tag = "Pre + Post Workflow",
      clinicalSummary = "52-year-old male with MRSA bacteremia and normal baseline renal function. Receiving Vancomycin 1000 mg IV q12h. Both peak and trough measured.",
      input = TdmInput(
        workflow = TdmWorkflow.PRE_POST,
        patient = Patient(
          patientId = "HUKM-2026-881",
          patientName = "Encik Ahmad Razali",
          ageYears = 52.0,
          gender = Gender.MALE,
          heightCm = 172.0,
          weightKg = 72.0,
          serumCreatinine = 85.0,
          creatinineUnit = CreatinineUnit.UMOL_L
        ),
        regimen = DosingRegimen(
          doseMg = 1000.0,
          intervalHours = 12.0,
          infusionDurationHours = 1.0
        ),
        targetIndication = TargetIndication.SEVERE_MRSA,
        micMgL = 1.0,
        preDoseConcMgL = 12.4,
        preDoseSampleDelayHoursBeforeNextDose = 0.5,
        postDoseConcMgL = 28.5,
        postDoseSampleDelayHoursAfterInfusionEnd = 1.0
      )
    ),
    FictionalCase(
      id = "case_2",
      title = "Case 2: Geriatric AKI / Renal Impairment (Pre Only)",
      tag = "Pre Workflow",
      clinicalSummary = "74-year-old female with hospital-acquired pneumonia and reduced renal clearance. Prescribed Vancomycin 750 mg IV q24h. Trough sample collected.",
      input = TdmInput(
        workflow = TdmWorkflow.PRE,
        patient = Patient(
          patientId = "HRPZ-2026-319",
          patientName = "Puan Fatimah Daud",
          ageYears = 74.0,
          gender = Gender.FEMALE,
          heightCm = 155.0,
          weightKg = 54.0,
          serumCreatinine = 165.0,
          creatinineUnit = CreatinineUnit.UMOL_L
        ),
        regimen = DosingRegimen(
          doseMg = 750.0,
          intervalHours = 24.0,
          infusionDurationHours = 1.5
        ),
        targetIndication = TargetIndication.MILD_MODERATE,
        micMgL = 1.0,
        preDoseConcMgL = 16.8,
        preDoseSampleDelayHoursBeforeNextDose = 1.0
      )
    ),
    FictionalCase(
      id = "case_3",
      title = "Case 3: ICU Polytrauma / High Clearance (Post Only)",
      tag = "Post Workflow",
      clinicalSummary = "28-year-old male with deep wound infection and Augmented Renal Clearance (ARC). Vancomycin 1500 mg IV q12h. Peak sample obtained.",
      input = TdmInput(
        workflow = TdmWorkflow.POST,
        patient = Patient(
          patientId = "HKL-2026-904",
          patientName = "Chong Wei Lun",
          ageYears = 28.0,
          gender = Gender.MALE,
          heightCm = 180.0,
          weightKg = 82.0,
          serumCreatinine = 52.0,
          creatinineUnit = CreatinineUnit.UMOL_L
        ),
        regimen = DosingRegimen(
          doseMg = 1500.0,
          intervalHours = 12.0,
          infusionDurationHours = 2.0
        ),
        targetIndication = TargetIndication.SEVERE_MRSA,
        micMgL = 1.0,
        postDoseConcMgL = 33.2,
        postDoseSampleDelayHoursAfterInfusionEnd = 1.0
      )
    )
  )
}
